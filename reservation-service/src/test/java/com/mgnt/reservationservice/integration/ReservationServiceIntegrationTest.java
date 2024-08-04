package com.mgnt.reservationservice.integration;

import com.mgnt.core.enums.ReservationStatus;
import com.mgnt.core.event.concert_service.InventoryReservationRequestEvent;
import com.mgnt.core.event.reservation_service.ReservationInventoryCreateResponseDTO;
import com.mgnt.core.exception.CustomException;
import com.mgnt.core.util.JsonUtil;
import com.mgnt.reservationservice.config.RedisConfig;
import com.mgnt.reservationservice.controller.dto.request.ReservationRequest;
import com.mgnt.reservationservice.domain.entity.Reservation;
import com.mgnt.reservationservice.domain.repository.ReservationRedisRepository;
import com.mgnt.reservationservice.domain.repository.ReservationRepository;
import com.mgnt.reservationservice.domain.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.mgnt.core.constants.Constants.ALL_RESERVATION_KEY;
import static com.mgnt.core.constants.Constants.RESERVATION_INCR_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {
        "eureka.client.enabled=false" // Eureka 클라이언트 비활성화
})
@ContextConfiguration(initializers = ReservationServiceIntegrationTest.Initializer.class)
@Import(RedisConfig.class)
public class ReservationServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceIntegrationTest.class);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @Container
    public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.31"))
            .withDatabaseName("ticketing_db")
            .withUsername("root")
            .withPassword("root")
            .withInitScript("init.sql");

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.5"))
            .withExposedPorts(6379)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1));

    private final ReservationServiceImpl reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationRedisRepository reservationRedisRepository;
    private final RedisTemplate<String, String> redisTemplate;
    @MockBean
    private KafkaTemplate<String, InventoryReservationRequestEvent> kafkaTemplate;

    @Autowired
    public ReservationServiceIntegrationTest(ReservationServiceImpl reservationService,
                                             ReservationRepository reservationRepository,
                                             ReservationRedisRepository reservationRedisRepository,
                                             RedisTemplate<String, String> redisTemplate) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationRedisRepository = reservationRedisRepository;
        this.redisTemplate = redisTemplate;
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + mysqlContainer.getJdbcUrl(),
                    "spring.datasource.username=" + mysqlContainer.getUsername(),
                    "spring.datasource.password=" + mysqlContainer.getPassword(),
                    "spring.data.redis.host=" + redisContainer.getHost(),
                    "spring.data.redis.port=" + redisContainer.getFirstMappedPort()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @BeforeAll
    public static void setUpBeforeAll() {
        System.setProperty("logging.level.root", "ERROR");
        System.setProperty("logging.level.org.springframework", "ERROR");
        System.setProperty("logging.level.com.mgnt.reservationservice", "ERROR");
        System.setProperty("logging.level.org.redisson", "ERROR");
    }

    @BeforeEach
    public void setUp() {
        // Ensure containers are running
        mysqlContainer.start();
        redisContainer.start();

        // Redis 초기 데이터 설정
        initializeRedisData();
    }

    private void initializeRedisData() {
        // Redis에서 기존의 모든 예약 키 삭제
        redisTemplate.delete("all_reservations");
        redisTemplate.delete("reservation_incr_key");

        // MySQL에서 모든 예약 정보 가져오기
        List<Reservation> allReservations = reservationRepository.findAll();

        // 예약 정보를 맵으로 변환 (key: 예약 ID, value: 예약 정보 JSON 문자열)
        Map<String, String> reservationMap = allReservations.stream()
                .collect(Collectors.toMap(
                        reservation -> reservation.getReservationId().toString(),
                        JsonUtil::convertToJson
                ));

        // 예약 정보를 Redis에 저장
        if (!reservationMap.isEmpty()) {
            redisTemplate.opsForHash().putAll("all_reservations", reservationMap);
        }

        // 가장 큰 reservationId를 찾아서 Redis의 next_id로 설정 (없으면 0으로 초기화)
        Long maxReservationId = allReservations.stream()
                .mapToLong(Reservation::getReservationId)
                .max()
                .orElse(0L);
        redisTemplate.opsForValue().set("reservation_incr_key", String.valueOf(maxReservationId));
    }

//    @ParameterizedTest
//    @ValueSource(ints = {50000}) // 테스트할 예약 개수를 100으로 줄임
//    public void testCreateReservations(int numReservations) throws InterruptedException {
//        int numThreads = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
//        CountDownLatch latch = new CountDownLatch(numReservations);
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger failCount = new AtomicInteger(0);
//        AtomicInteger processedCount = new AtomicInteger(0);
//
//        log.info("Starting reservation creation test with {} reservations", numReservations);
//
//        for (int i = 1; i <= numReservations; i++) {
//            final int index = i;
//            executor.submit(() -> {
//                try {
//                    Long userId = (long) index;
//                    Long seatId = (long) (index % 20 + 1); // 20개의 좌석을 가정
//                    ReservationRequest request = new ReservationRequest(
//                            1L, // concertId
//                            1L, // concertDateId
//                            seatId,
//                            BigDecimal.valueOf(5000),
//                            ReservationStatus.ING,
//                            ZonedDateTime.now().plusDays(1)
//                    );
//
//                    log.debug("Attempting to create reservation for user {} and seat {}", userId, seatId);
//                    ReservationInventoryCreateResponseDTO response = reservationService.createReservationWithoutPayment(userId, request);
//
//                    assertNotNull(response);
//                    log.debug("Successfully created reservation with ID: {}", response.reservationId());
//                    successCount.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Failed to create reservation for index {}", index, e);
//                    failCount.incrementAndGet();
//                } finally {
//                    latch.countDown();
//                    int processed = processedCount.incrementAndGet();
//                    if (processed % 10 == 0 || processed == numReservations) {
//                        log.info("Processed {} out of {} reservations", processed, numReservations);
//                    }
//                }
//            });
//        }
//
//        boolean completed = latch.await(10, TimeUnit.MINUTES);
//        executor.shutdown();
//        executor.awaitTermination(1, TimeUnit.MINUTES);
//
//        if (!completed) {
//            log.warn("Test did not complete within the time limit");
//        }
//
//        long redisCount = reservationRedisRepository.countAllReservations();
//        long mysqlCount = reservationRepository.count();
//        String lastRedisReservationId = reservationRedisRepository.get(RESERVATION_INCR_KEY);
//        Long lastMysqlReservationId = mysqlCount;
//
//        log.info("Test Results:");
//        log.info("Total attempts: {}", numReservations);
//        log.info("Successful reservations: {}", successCount.get());
//        log.info("Failed reservations: {}", failCount.get());
//        log.info("Redis count: {}", redisCount);
//        log.info("MySQL count: {}", mysqlCount);
//        log.info("Last Redis Reservation ID: {}", lastRedisReservationId);
//        log.info("Last MySQL Reservation ID: {}", lastMysqlReservationId);
//
//        assertEquals(successCount.get(), redisCount, "Redis reservation count should match the number of successful reservations");
//        assertEquals(successCount.get(), mysqlCount, "MySQL reservation count should match the number of successful reservations");
//        assertEquals(numReservations, successCount.get() + failCount.get(), "Total attempts should equal numReservations");
//        assertEquals(lastRedisReservationId, String.valueOf(lastMysqlReservationId), "Last Reservation ID should be the same in Redis and MySQL");
//    }
}

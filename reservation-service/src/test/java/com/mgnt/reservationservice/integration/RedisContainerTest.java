package com.mgnt.reservationservice.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Testcontainers
public class RedisContainerTest {

    private static final Logger logger = LoggerFactory.getLogger(RedisContainerTest.class);
    private static final int PORT = 6379;

    @Container
    public static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2.5"))
            .withExposedPorts(PORT)
            .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*\\n", 1))
            .withLogConsumer(outputFrame -> logger.info(outputFrame.getUtf8String()));

    private static StringRedisTemplate redisTemplate;

    @BeforeClass
    public static void before() {
        redisContainer.start();
        logger.info("Redis container started");

        String containerHost = redisContainer.getHost();
        int containerPort = redisContainer.getMappedPort(PORT);

        // Configure the RedisTemplate
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(containerHost, containerPort);
        lettuceConnectionFactory.afterPropertiesSet();

        redisTemplate = new StringRedisTemplate(lettuceConnectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    @AfterClass
    public static void after() {
        redisContainer.stop();
        logger.info("Redis container stopped");
    }

    @Test
    public void testSetAndGetValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("name", "Himanshu");
        String value = ops.get("name");
        assertEquals("Himanshu", value);
    }

    @Test
    public void testUpdateValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("name", "John");
        String value = ops.get("name");
        assertEquals("John", value);

        ops.set("name", "Doe");
        value = ops.get("name");
        assertEquals("Doe", value);
    }

    @Test
    public void testDeleteValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("name", "Alice");
        String value = ops.get("name");
        assertEquals("Alice", value);

        redisTemplate.delete("name");
        value = ops.get("name");
        assertNull(value);
    }

    @Test
    public void testExpiration() throws InterruptedException {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("tempKey", "tempValue", 1, TimeUnit.SECONDS);
        String value = ops.get("tempKey");
        assertEquals("tempValue", value);

        // Wait for key to expire
        Thread.sleep(2000);
        value = ops.get("tempKey");
        assertNull(value);
    }

    @Test
    public void testAppendValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("greet", "Hello");
        ops.append("greet", " World");
        String value = ops.get("greet");
        assertEquals("Hello World", value);
    }

    @Test
    public void testIncrementValue() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("counter", "1");
        Long value = ops.increment("counter");
        assertEquals(Long.valueOf(2), value);

        value = ops.increment("counter", 5);
        assertEquals(Long.valueOf(7), value);
    }
}

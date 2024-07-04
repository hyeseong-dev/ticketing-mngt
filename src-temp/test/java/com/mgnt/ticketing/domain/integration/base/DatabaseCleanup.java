//package com.mgnt.ticketing.domain.integration.base;
//
//import com.google.common.base.CaseFormat;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//@ActiveProfiles("test")
//public class DatabaseCleanup implements InitializingBean {
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Autowired
//    private Environment env;
//
//    private List<String> tableNames;
//
//    @Override
//    public void afterPropertiesSet() {
//        tableNames = entityManager.getMetamodel().getEntities().stream()
//                .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
//                .map(e -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, e.getName()))
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public void execute() {
//        entityManager.flush();
//
//        if (isTestProfile()) {
//            // test 프로파일이 활성화되었을 때 H2 데이터베이스를 위한 처리
//            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
//
//            for (String tableName : tableNames) {
//                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
//                // H2 데이터베이스의 경우, AUTO_INCREMENT를 직접 리셋할 수 있는 명령어가 없으므로 이 부분은 생략
//            }
//
//            entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
//        } else {
//            // test 프로파일이 아닐 때, 기본 로직 실행
//            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
//
//            for (String tableName : tableNames) {
//                entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
//                entityManager.createNativeQuery("ALTER TABLE " + tableName + " AUTO_INCREMENT = 1").executeUpdate();
//            }
//
//            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
//        }
//    }
//
//    public boolean isTestProfile() {
//        // 현재 활성화된 프로파일 확인
//        String[] activeProfiles = env.getActiveProfiles();
//        boolean isTestProfileActive = false;
//        for (String profile : activeProfiles) {
//            if ("test".equals(profile)) {
//                isTestProfileActive = true;
//                break;
//            }
//        }
//
//        return isTestProfileActive;
//    }
//}
//

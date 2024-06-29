FROM openjdk:17-jdk-slim

ENV TZ=Asia/Seoul

# 컨테이너 내부에서 애플리케이션 파일을 저장할 디렉토리를 생성합니다.
WORKDIR /app

# 빌드된 JAR 파일을 현재 위치에서 컨테이너의 /app 디렉토리로 복사합니다.
COPY build/libs/my-spring-boot-app-0.0.1-SNAPSHOT.jar /app/my-app.jar

# 기본값으로 'dev'를 설정합니다.
ENV ACTIVE_PROFILES=dev

# 애플리케이션을 실행합니다.
ENTRYPOINT ["java", "-jar", "/app/my-app.jar", "--spring.profiles.active=${ACTIVE_PROFILES}"]
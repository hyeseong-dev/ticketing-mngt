# 빌더 이미지
FROM gradle:8.8.0-jdk17 AS builder

# 필요한 폴더 생성
RUN mkdir -p /app

WORKDIR /app
COPY . /app
RUN gradle clean build

# 프로덕션 이미지
FROM openjdk:17-jdk-slim

# 타임존 설정
ENV TZ=Asia/Seoul

# 기본값으로 'dev' 설정
ENV ACTIVE_PROFILES=dev

# 작업 디렉토리 설정
WORKDIR /app

# 빌더 스테이지에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/ticketing-0.0.1-SNAPSHOT.jar /app/ticketing.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/ticketing.jar", "--spring.profiles.active=${ACTIVE_PROFILES}"]

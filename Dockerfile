#
# Base phase for common dependencies
#
FROM amazoncorretto:17-alpine AS base
RUN apk add --no-cache bash dos2unix

WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew

# Gradle 설정 파일 복사 및 Gradle Wrapper 설치
COPY build.gradle settings.gradle ./
COPY config/checkstyle/google_checks.xml config/checkstyle/google_checks.xml

# Gradle 종속성 다운로드
COPY gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar
RUN ./gradlew --version

#
# Dependencies phase
#
FROM base AS dependencies
RUN ./gradlew dependencies

#
# Build phase
#
FROM dependencies AS build
COPY src src
RUN ./gradlew build -x test

# JAR 파일을 Docker 친화적인 구조로 추출
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

#
# Final runtime phase
#
FROM amazoncorretto:17-alpine AS prod

WORKDIR /app

# 앱 실행에 필요한 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod

# build 단계에서 빌드된 애플리케이션 파일 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]

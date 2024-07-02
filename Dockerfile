#
# Dev phase
#
FROM amazoncorretto:17-alpine AS dev
RUN apk add --no-cache bash dos2unix

# 프로젝트 디렉토리 생성
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN dos2unix ./gradlew
RUN chmod +x ./gradlew

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY config/checkstyle/google_checks.xml config/checkstyle/google_checks.xml

# Gradle 종속성 다운로드
COPY --chown=gradle:gradle gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.properties
COPY --chown=gradle:gradle gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.jar

# Gradle Wrapper 사용
RUN ./gradlew --version
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

#
# Prod-build phase
#
FROM dev AS build

# 프로덕션 환경 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 빌드
RUN ./gradlew build -x test --no-daemon

# 생성된 JAR 파일을 더 Docker 친화적인 구조로 추출
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

#
# Prod-deploy phase
#
FROM amazoncorretto:17-alpine AS prod

WORKDIR /app

# 앱 실행에 필요한 환경 변수 설정
ENV SPRING_PROFILES_ACTIVE=prod

# build 단계에서 빌드된 애플리케이션 파일 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]

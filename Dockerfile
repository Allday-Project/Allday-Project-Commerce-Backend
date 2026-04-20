
# JDK 21 환경 기반 이미지 사용
FROM eclipse-temurin:21-jdk

# JAR 파일을 컨테이너 내부로 복사 (이름 자동 매칭) -> 사전 준비에서 생성한 jar 파일 기준으로 적용
COPY build/libs/allday-project-commerce-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너 실행 시 수행할 명령
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 외부 접근을 위한 포트 설정
EXPOSE 8090
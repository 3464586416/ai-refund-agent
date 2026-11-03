# ---- 构建阶段 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
# 先拉依赖单独成层，源码改动不重复下载
RUN mvn -B -q dependency:go-offline || true
COPY src ./src
RUN mvn -B -DskipTests package \
 && cp target/ZNTP-0.0.1-SNAPSHOT.jar app.jar

# ---- 运行阶段 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/app.jar app.jar
EXPOSE 7860
ENV TZ=Asia/Shanghai
# Hugging Face Space 会把 PORT 设为 7860；本地 docker run 不带 PORT 时默认 7860
ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=70 -jar app.jar --server.port=${PORT:-7860}"]

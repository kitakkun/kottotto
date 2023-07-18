FROM openjdk:17
RUN microdnf install findutils

WORKDIR /app

COPY . /app
RUN chmod +x /app/gradlew
RUN /app/gradlew

RUN /app/gradlew build

CMD ["/app/gradlew", "run"]

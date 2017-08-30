FROM hseeberger/scala-sbt:8u141-jdk_2.12.3_0.13.16 as builder

COPY . /autoscaler

RUN cd /autoscaler && \
    sbt assembly

FROM openjdk:8-jre

COPY --from=builder /autoscaler/target/scala-2.12/autoscaler-assembly-0.1.jar /autoscaler.jar

ENTRYPOINT ["java", "-jar", "/autoscaler.jar"]

CMD ["--min", "1", "--max", "5", "--period", "60"]

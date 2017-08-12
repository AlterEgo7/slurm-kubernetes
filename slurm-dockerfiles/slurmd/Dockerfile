FROM hseeberger/scala-sbt:8u141-jdk_2.12.3_0.13.16 as builder

COPY slurm-kubernetes-scripts /slurm-kubernetes-scripts

RUN cd /slurm-kubernetes-scripts && \
    sbt assembly


FROM localhost:5000/slurm-base:17.02.6

RUN apt-get update && apt-get install -y --no-install-recommends openjdk-8-jre-headless
COPY slurmd.conf /etc/supervisor/conf.d/slurmd.conf
COPY --from=builder /slurm-kubernetes-scripts/target/scala-2.12/slurm-kubernetes-scripts-assembly-1.0.jar /hooks.jar

COPY liveness.sh /liveness.sh
COPY preStop.sh /preStop.sh

CMD /usr/bin/supervisord --nodaemon

FROM localhost:5000/slurm-base:17.02.6

RUN apt-get update && apt-get install -y openssh-server cython inotify-tools socat
RUN mkdir /var/run/sshd
RUN echo 'slurm:slurm' | chpasswd
RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
RUN echo "export VISIBLE=now" >> /etc/profile
ADD sshd.conf /etc/supervisor/conf.d/sshd.conf

COPY slurmctld.conf /etc/supervisor/conf.d/slurmctld.conf
COPY config-reload.sh /config-reload.sh
COPY config-watch.conf /etc/supervisor/conf.d/config-watch.conf
COPY drain-listen.conf /etc/supervisor/conf.d/drain-listen.conf
COPY drain_host.sh /drain_host.sh
COPY liveness.sh /liveness.sh
COPY slurm-info.conf /etc/supervisor/conf.d/slurm-info.conf
COPY slurm-info.sh /slurm-info.sh

RUN chmod +x /drain_host.sh
RUN chmod +x /config-reload.sh
RUN chmod +x /slurm-info.sh

EXPOSE 22

CMD /usr/bin/supervisord --nodaemon

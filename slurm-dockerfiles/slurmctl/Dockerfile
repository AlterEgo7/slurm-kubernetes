FROM localhost:5000/slurm-base:17.02.6

COPY slurmctld.conf /etc/supervisor/conf.d/slurmctld.conf

RUN chown -R root:slurm /var/spool && \
    chmod -R g+w /var/spool

RUN apt-get update && apt-get install -y openssh-server
RUN mkdir /var/run/sshd
RUN echo 'slurm:slurm' | chpasswd
RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
RUN echo "export VISIBLE=now" >> /etc/profile
ADD sshd.conf /etc/supervisor/conf.d/sshd.conf

COPY liveness.sh /liveness.sh

EXPOSE 22

CMD /usr/bin/supervisord --nodaemon

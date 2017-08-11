FROM localhost:5000/slurm-base:17.02.6

COPY slurmctld.conf /etc/supervisor/conf.d/slurmctld.conf
COPY config-reload.sh /config-reload.sh
COPY config-watch.conf /etc/supervisor/conf.d/config-watch.conf

RUN apt-get update && apt-get install -y openssh-server cython inotify-tools
RUN mkdir /var/run/sshd
RUN echo 'slurm:slurm' | chpasswd
RUN sed -i 's/PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
RUN echo "export VISIBLE=now" >> /etc/profile
ADD sshd.conf /etc/supervisor/conf.d/sshd.conf

RUN git clone --depth=1 https://github.com/PySlurm/pyslurm.git && \
    cd pyslurm && \
    python setup.py build --slurm-lib=/usr/local/lib --slurm-inc=/usr/local/include && \
    python setup.py install

COPY pending_queue_size.py /

COPY liveness.sh /liveness.sh

EXPOSE 22

CMD /usr/bin/supervisord --nodaemon

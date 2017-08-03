FROM localhost:5000/slurm-base:17.02.6

COPY slurmd.conf /etc/supervisor/conf.d/slurmd.conf

COPY liveness.sh /liveness.sh

CMD /usr/bin/supervisord --nodaemon

FROM ubuntu:16.04

ENV SLURM_VERSION=17.02.6

RUN useradd slurm

RUN apt-get update && apt install -y munge curl gcc make bzip2 supervisor python python-dev \
    libmunge-dev libmunge2 lua5.3 lua5.3-dev libopenmpi-dev openmpi-bin \
    gfortran vim python-mpi4py python-numpy python-psutil sudo psmisc \
    software-properties-common python-software-properties iputils-ping wget

RUN curl -fsL https://www.schedmd.com/downloads/latest/slurm-${SLURM_VERSION}.tar.bz2 | tar xfj - -C /opt/ && \
    cd /opt/slurm-${SLURM_VERSION}/ && \
    ./configure && make && make install

COPY slurm.conf /usr/local/etc/slurm.conf

RUN mkdir /var/lib/slurm

RUN create-munge-key -f && \
    mkdir /var/run/munge && \
    chown root /var/lib/munge && \
    chown root /etc/munge/munge.key && \
    chown root /etc/munge && chmod 600 /var/run/munge && \
    chmod 755  /run/munge && \
    chmod 600 /etc/munge/munge.key

COPY munged.conf /etc/supervisor/conf.d/munged.conf

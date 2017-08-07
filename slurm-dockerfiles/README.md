# Slurm Dockerfiles

## Purpose

This directory holds the Dockerfiles and associated configuration files for building the docker images for slurm-master and slurm-workers.

## Usage

Build the base image from the Dockerfile in this directory. This base image is extended by the slurmd and slurmctl Dockerfiles in the respective directories.

Note that both the master and worker images used must extend the same base image, otherwise the munge key will be different and the workers won't be able to authenticate to the master.

## Notes

* Dockerfiles build slurm from source. This takes some time.
* Images have not been optimized for size yet.
* The `slurm` user SSH password is set by default to *slurm*, however this should be overridden for security purposes.

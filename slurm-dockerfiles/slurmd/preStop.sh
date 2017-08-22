#!/bin/bash

HOSTNAME="$(hostname -f)"

# Drain host
echo $HOSTNAME | nc slurm-master.slurm 22345

timeout 30 bash -c 'until [ "$(sinfo -h -n $(hostname -f) | awk '\''{ print $5 }'\'')" = "drain" ]; do sleep 5; done'

java -jar /hooks.jar preStop

# Issue reconfigure to remove host
echo $HOSTNAME | nc slurm-master.slurm 22346

#!/bin/bash

read WORKER_HOST
supervisorctl restart slurmctld > /dev/null
scontrol reconfigure

status="$(sinfo -n $WORKER_HOST -h | awk '{ print $5 }')"

if ["$status" = "drain" ]
then
  scontrol update nodename=$WORKER_HOST state=resume
fi

echo "$(date +'%F %T'): Slurm configuration updated"
#!/bin/bash

inotifywait -mqr -e close /usr/local/etc |
while read -r directory events filename; do
  if [ "$filename" = "slurm.conf" ]; then
    supervisorctl restart slurmctld
    scontrol reconfigure
    echo "$(date +'%F %T'): Slurm configuration updated"
  fi
done
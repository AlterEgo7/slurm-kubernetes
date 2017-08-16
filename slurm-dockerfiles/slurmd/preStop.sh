#!/bin/bash

echo "$(hostname -f)" | nc slurm-master.slurm 22345

timeout 30 bash -c 'until [ "$(sinfo -h -n $(hostname -f) | awk '\''{ print $5 }'\'')" = "drain" ]; do sleep 5; done'

java -jar /hooks.jar preStop

#!/bin/bash

echo "$(hostname -f)" | nc slurm-master.slurm 22345
sleep 30
java -jar /hooks.jar preStop

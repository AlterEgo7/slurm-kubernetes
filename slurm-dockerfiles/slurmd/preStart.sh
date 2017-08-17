#!/bin/bash

java -jar /hooks.jar preStart > hooks.log 2>&1

echo "$(hostname -f)" | nc slurm-master.slurm 22346

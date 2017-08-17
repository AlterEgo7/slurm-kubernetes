#!/bin/bash

supervisorctl restart slurmctld
scontrol reconfigure
echo "$(date +'%F %T'): Slurm configuration updated"
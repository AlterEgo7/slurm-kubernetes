#!/bin/bash

status="$(supervisorctl status slurmctld | awk '{ print $2 }')"
if [ "$status" = "RUNNING" ]
then
  exit 0
else
  exit 1
fi

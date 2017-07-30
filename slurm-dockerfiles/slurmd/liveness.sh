#!/bin/bash

status="$(supervisorctl status slurmd | awk '{ print $2 }')"
if [ "$status" = "RUNNING" ]
then
  exit 0
else
  exit 1
fi

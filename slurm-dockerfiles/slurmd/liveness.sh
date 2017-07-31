#!/bin/bash

hostname="$(hostname -f)"
status="$(sinfo -h -n $hostname | awk '{ print $5 }')"
if [ "$status" = "idle" ] || [ "$status" = "alloc"]
then
  exit 0
else
  exit 1
fi


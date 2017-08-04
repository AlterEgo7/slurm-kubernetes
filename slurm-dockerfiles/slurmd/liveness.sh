#!/bin/bash

hostname="$(hostname -f)"
status="$(sinfo -h -n $hostname --dead | awk '{ print $4 }')"

exit $status

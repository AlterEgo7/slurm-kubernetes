#!/bin/bash

set -e
hostname="$(hostname -f)"
sinfo="$(sinfo -h -n $hostname --dead)"
status="$($sinfo | awk '{ print $4 }')"

exit $status

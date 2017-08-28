#!/bin/bash

queue_size=$(squeue -arh -t pd | wc -l)
alloc_nodes=$(sinfo -t alloc -h | awk '{ print $4 }')

printf '{"queue_size": %s, "alloc_nodes": %s}\n' "$queue_size" "$alloc_nodes"
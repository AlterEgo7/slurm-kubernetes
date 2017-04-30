#!/bin/bash

for i in {1..3}
do
	vagrant snapshot save "cluster-node-$i" "$1-$i"
done
#!/bin/bash

for i in {1..3}
do
	vagrant snapshot restore "cluster-node-$i" "$1-$i"
done
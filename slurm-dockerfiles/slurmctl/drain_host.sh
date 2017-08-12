#!/bin/bash

read WORKER_HOST
scontrol update nodename=$WORKER_HOST state=drain reason="scaling"

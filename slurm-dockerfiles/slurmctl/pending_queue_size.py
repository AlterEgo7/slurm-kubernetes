#!/usr/bin/env python

from __future__ import print_function

import pyslurm
import sys

from time import gmtime, strftime, sleep

def get_job_size(job):
  if 'array_task_str' in job:
    low, high = job['array_task_str'].split('-')
    return int(high) - int(low) + 1
  else:
    return 1

if __name__ == "__main__":
  a = pyslurm.job()
  jobs = a.get()
  queue_length = sum({k: get_job_size(v) for k, v in jobs.iteritems() if v['job_state'] == 'PENDING' }.values())
  print(queue_length)
  
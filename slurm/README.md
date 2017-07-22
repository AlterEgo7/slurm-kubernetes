# Slurm Kubernetes

## Slurm cluster managed by Kubernetes

**Slurm Kubernetes** creates a slurm cluster in Kubernetes. Workers are deployed as a stateful set, and access to slurm master is possible through SSH, on port 22222 by default on the Kubernetes master. 

## Extra Prerequisites

The scripts expect to find the Slurm docker images on the Kubernetes cluster. At the moment these are custom Dockerfiles located at [slurm-dockerfiles](https://github.com/AlterEgo7/cluster-management/tree/master/slurm-dockerfiles).

The easiest way to make this work, is to deploy a private registry on localhost, build the dockerfiles, upload them to the local registry, and add the registry to docker daemon configuration for the Kubernetes cluster. This is already set up in the default settings together with a registry cache at [k8s-cluster.yml](https://github.com/AlterEgo7/cluster-management/blob/master/slurm/inventory/group_vars/k8s-cluster.yml).

## Usage

The Ansible scripts can be invoked with the following command:

```ansible-playbook -b -i inventory cluster.yml```

This will in turn install the following:

* Kubernetes
* Heapster
* Glusterfs
* Slurm

## Notes

* Glusterfs pods and services are created in the "glusterfs" namespace.
* Slurm pods and services are created in the "default" namespace for easier monitoring from the terminal.
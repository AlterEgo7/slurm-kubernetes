# Thesis Cluster Management

## Purpose

The purpose of this repo is to hold ansible files for automating cluster creation scripts for multiple cluster management and container orchestration systems.

## Notes

* At the moment all clusters are created using Vagrant. Provisioning of the cluster nodes is done through Ansible playbooks, making the provisiong independent of the underlying platform on which the nodes are deployed. Hopefully, just changing the hosts to point to Cloud Platform VMs will be the only step needed to transition from Vagrant to the cloud.

* All 3 clusters are based on the official Vagrant Ubuntu 16.04 image for VirtualBox, namely 'ubuntu/xenial64'.

* At the moment, in the Vagrantfiles the SSH key is passed in the authorized_keys verbatim. For Ansible to work, passwordless SSH login with the ubuntu user is needed.
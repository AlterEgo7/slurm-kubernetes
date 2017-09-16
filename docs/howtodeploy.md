
# How to deploy?

## Pre-conditions

- set of machines running Linux
- accessible via ssh for root
- publickey authentication setup

## Set Ansible variables

Edit `slurm/inventory/inventory` and replace:
  - ansible_host by short hostname (first element of FQDN)
  - ansible_user by root
  - ip by VM/machine ip addresses
  - propagate new ansible_host hostname in the following sections
    - [kube-master]
    - [etcd]
    - [kube-node]

## Set GlusterFS disk space

- Find out
- replace `/dev/sdc` with the block device you want to use on the VMs (ex: `/dev/xvdb`) in:
  - each N of inventory/host_vars/glusterN.yml
- `heketi` will create a logical volume in the volume provided
- :warning: the block device provided **must be free of any logical volume created by LVM**

## Clean what's been deployed

- In case you want to restore the machine to its initial state, you can try running the `slurm/reset.yaml` ansible playbook
- This will remove the block device created by GlusterFS


## Misc

- `roles/slurm/templates/slurm.yaml.j2:  name: gluster1`
  - does not need to be changed => Persistent Volume Claim, the name is arbitrary


## Troubleshooting

### Kubernetes

- kubernetes master should see all the other nodes as Ready
  - `kubectl get nodes`
  - `kubectl -n kube-system get all`

- don't use FQDN but simple hostnames!
  - all tagged commands run from master nodes

### Slurm
- check slurm is running
  - `kubectl exec -it slurm-master bash`
  - `Error from server (BadRequest): pod slurm-master does not have a host assigned`

- change something => kill pods
  - needs to be done in 3 steps to avoid PVC being removed too early
  - ```bash
    kubectl delete statefulset slurm --grace-period=0
    kubectl delete pod slurm-master
    kubectl delete -f /tmp/slurm/slurm.yaml
    ```

- change config in config without going through ansible (from kube master)
`kubectl apply -f /tmp/slurm/slurm-config.yaml`

### GlusterFS

```bash
kubectl -n kube-system
kubectl -n kube-system get service
export HEKETI_CLI_SERVER=http://10.233.38.197:8080
heketi-cli topology load --json
heketi-cli topology load --json=/tmp/glusterfs/gluster-kubernetes-1.1/deploy/topology.json
```

### When rebooting host machines

- restart the following services in this order:
  - `docker`
  - `etcd`
  - `kubelet`

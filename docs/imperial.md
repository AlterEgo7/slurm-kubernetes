
# Deploying the cluster in Imperial College's DoC cloud

## Setup machines
- spawn machines in DoC's cloud using [this script])()

`bash -x new_machine_from_template.sh gluster${i}`

## SSH login
- can't use SSH keypair for some reason => ask CSG
- manually copy SSH key to root user /root/.ssh/authorized_keys
- TODO fix this

## Block devices

- in DoC's cloud, attached shared storage are available as `/dev/xvdb`

### From cloudstack disk to empty block device for glusterFS

This is the recipe employed to "clean" newly attached shared storage:

```shell
umount /dev/mapper/datavg-datalv
lvremove /dev/datavg/datalv
vgremove datavg
pvscan --cache
modprobe dm-thin-pool # should have been already been run by ansible playbook
```

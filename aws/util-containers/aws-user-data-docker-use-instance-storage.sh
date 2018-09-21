#!/bin/bash

# Sam note: lightly modified from https://gist.github.com/kamermans/176c190670a163b1147e
# For setting up a docker system with instance stores.
# Note: you must manually set up the instance stores for the instance on launch to have
# Volumes as /dev/sdb and /dev/sdc
# This will enable c3.* AWS instances to be used as high-performance build agents with many 
# executors

# Original comments
# This script will DESTROY /dev/xvdb and /dev/xvdc and remount them
#   for Docker temp and volume storage.
# It is intended for EC2 instances with 2 ephemeral SSD instance stores
#   like the c3.xlarge instance type.
yum install -y docker
service docker stop || true

# Setup Instance Store 0 for Docker Temp
#   (set in /etc/default/docker)
DEV="/dev/sdb"
umount $DEV 2>/dev/null || true
mkdir /mnt/docker-temp 2>/dev/null || rm -rf /mnt/docker-temp/*
mkfs.ext4 $DEV
mount -t ext4 -o noatime $DEV /mnt/docker-temp

# Setup Instance Store 1 for Docker volume storage
DEV="/dev/sdc"
umount $DEV 2>/dev/null || true
mkdir /mnt/docker-volumes 2>/dev/null || rm -rf /mnt/docker-volumes/*
mkfs.ext4 $DEV
rm -rf /var/lib/docker/vfs
rm -rf /var/lib/docker/volumes
mount -t ext4 -o noatime $DEV /mnt/docker-volumes
mkdir /mnt/docker-volumes/vfs
ln -s /mnt/docker-volumes/vfs /var/lib/docker/vfs
mkdir /mnt/docker-volumes/volumes
ln -s /mnt/docker-volumes/volumes /var/lib/docker/volumes

service docker start

docker run --rm -d \
  --add-host jenkins:10.0.0.55 --add-host gitserver:10.0.0.13 \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors $(nproc) -description swarm-slave" \
  svanoort/jenkins-swarm-agent-mvn:1.0
#!/bin/bash
set -xe
set -o pipefail

# Create network if absent
if [ $(docker network ls | grep scalability-bridge | wc -l) -eq 0 ]; then
    docker network create --attachable -d bridge scalability-bridge
fi

# Obtain the block device name of Jenkins root, for use in resource limits and querying io stats
ROOT_BLKDEV_NAME=$(docker run --rm -it tutum/influxdb lsblk -d -o NAME | tail -n 1 | tr -d '\r' | tr -d '\n')
ROOT_BLKDEV="/dev/$ROOT_BLKDEV_NAME"

echo "BLOCK DEVICE ID IS $ROOT_BLKDEV"

# Separate container for graphana 4
# Ports 81 - grafana,
docker run --rm -d --network scalability-bridge \
  -e ROOT_BLKDEV_NAME=$ROOT_BLKDEV_NAME \
  -h grafana --name grafana \
  --add-host influx:10.0.0.168 \
  -p 8081:3000 \
  temp-grafana:1.0

# Launch our Jenkins instance or thereabouts
docker run --cap-add=SYS_PTRACE --rm -it -d -h jenkins --network scalability-bridge --name jenkins -l role=jenkins \
  --add-host influx:10.0.0.168 --add-host gitserver:10.0.0.168 \
  -p 8080:8080 -p 9011:9011 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  --device-write-iops $ROOT_BLKDEV:2000 --device-write-bps $ROOT_BLKDEV:200mb --device-read-iops $ROOT_BLKDEV:2000 --device-read-bps $ROOT_BLKDEV:200mb \
  temp-jenkins-scalability-master:1.0

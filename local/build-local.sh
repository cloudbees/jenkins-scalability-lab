#!/bin/bash
set -xe
set -o pipefail

# Build for local run

# Create SSH key if absent, set up for secret
if [ ! -f id_rsa ]; then
  ssh-keygen -t rsa -n "" -P "" -C "" -f id_rsa
fi

# Create network if absent
if [ $(docker network ls | grep scalability-bridge | wc -l) -eq 0 ]; then
    docker network create --attachable -d bridge scalability-bridge
fi

# Needed to pick up git configs and some other things
CONFIG_DIR=$(cd .. && pwd)
cp id_rsa.pub "${CONFIG_DIR}/gitserver/keys"
cp id_rsa* "${CONFIG_DIR}/jenkins"
docker build -t jenkins-scalability-master:2.0 ../jenkins
docker build -t temp-gitserver:1.0 ../gitserver

# When docker-compose networking is fixed to detect the bridge network this will work
# CONFIG_DIR=$(cd .. && pwd) docker-compose up

# Start git server, see keys from https://github.com/jkarlosb/git-server-docker
docker run --rm -d -p 2222:22 \
   --network scalability-bridge \
   -h gitserver \
   --name gitserver -l role=gitserver \
   -v "$CONFIG_DIR/gitserver/keys:/git-server/keys" \
   temp-gitserver:1.0
   #jkarlos/git-server-docker

# Graphite server
# External ports: 82 - graphite web interface with admin login admin:admin
# 7002 is carbon-cache query port used by grafana

# InfluxDB to ingest data, see port 8083 for web interface, accepts graphite input on port 2015
# Port 8086 is where Grafana binds to it
# Note: DB is my_db for stats, username and password below
# May need to play with the template because it truncates hostname
docker run -d --rm -h influx --name influx --network scalability-bridge \
 -p 8083:8083 -p 8086:8086 -p 2015:2015 \
 -e ADMIN_USER="root" -e INFLUXDB_INIT_PWD="somepassword" -e PRE_CREATE_DB=my_db \
 -e GRAPHITE_DB="my_db" -e GRAPHITE_BINDING=':2015' -e GRAPHITE_PROTOCOL="tcp" -e GRAPHITE_template="host.tag1.tag2.measurement*" tutum/influxdb

# Separate container for graphana 4 until we can build a custom Graphite-Grafana-Carbon-Cache image
# Ports 81 - grafana, 
docker run --rm -d --network scalability-bridge \
  -h grafana --name grafana \
  -p 81:3000 \
  grafana/grafana:4.3.2

ROOT_BLKDEV=/dev/$(docker run --rm -it jenkins-scalability-master:2.0 lsblk -d -o NAME | tail -n 1 | tr -d '\r' | tr -d '\n')

# Run jenkins, specifying a named volume makes it persistent even after container dies
docker run --rm -d -h jenkins --name jenkins -l role=jenkins --network scalability-bridge \
  --device-write-iops $ROOT_BLKDEV:200 --device-write-bps $ROOT_BLKDEV:100mb --device-read-iops $ROOT_BLKDEV:200 --device-read-bps $ROOT_BLKDEV:100mb \
  -p 8080:8080 -p 9011:9011 \
  -v jenkins_home:/var/jenkins_home \
  jenkins-scalability-master:2.0

# Autoconnects & creates agents
docker run --rm -d --network scalability-bridge \
  --name agent -l role=agent \
  -e "COMMAND_OPTIONS=-master http://jenkins:8080 -executors 4 -description swarm-slave -deleteExistingClients" \
  vfarcic/jenkins-swarm-agent

docker attach jenkins 

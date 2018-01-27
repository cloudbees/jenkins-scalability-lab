#!/bin/bash
# Launches containers to view the data

# TODO check for data dump argument or local dump

# Create a clean dump dir
rm -rf ./dump || true
mkdir dump
tar -xzf *.tar.gz -C ./dump


# FIXME: restore needs to run while influxd is not running?!
docker run -d --rm -h influx --name influx --network scalability-bridge \
 -p 8083:8083 -p 8086:8086 -p 2015:2015 \
 -e ADMIN_USER="root" -e INFLUXDB_INIT_PWD="somepassword" -e PRE_CREATE_DB=hydra \
 -e GRAPHITE_DB="hydra" -e GRAPHITE_BINDING=':2015' -e GRAPHITE_PROTOCOL="tcp" \
 -e GRAPHITE_template="measurement*" appcelerator/influxdb:influxdb-1.2.2

docker cp ./dump influx:/tmp/dump
docker exec influx influxd restore -metadir /data/meta /tmp/dump/hydra_influx_dump
docker exec influx influxd restore -database hydra -datadir /data/ /tmp/dump/hydra_influx_dump

# Separate container for graphana 4
# Ports 81 - grafana, 
docker run --rm -d --network scalability-bridge \
  -e ROOT_BLKDEV_NAME=$ROOT_BLKDEV_NAME \
  -h grafana --name grafana \
  -p 81:3000 \
  temp-grafana:1.0

# FIXME need a way to set time range to match the imported DB in Grafana
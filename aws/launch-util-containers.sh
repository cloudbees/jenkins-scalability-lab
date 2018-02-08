#!/bin/bash

# Setup
sudo yum install -y docker && sudo service docker start && sudo su

# Launch the influx instance
docker run -d --rm -h influx --name influx \
 -p 8083:8083 -p 8086:8086 -p 2015:2015 \
 -e ADMIN_USER="root" -e INFLUXDB_INIT_PWD="somepassword" -e PRE_CREATE_DB=hydra \
 -e GRAPHITE_DB="hydra" -e GRAPHITE_BINDING=':2015' -e GRAPHITE_PROTOCOL="tcp" \
 -e GRAPHITE_template="measurement*" appcelerator/influxdb:influxdb-1.2.2

 # Git server
docker run --rm -d -p 2222:2222 \
   -h gitserver \
   --name gitserver -l role=gitserver \
   -v "/tmp/keys:/git-server/keys" \
   svanoort/hydra-gitserver:remote
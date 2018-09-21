#!/bin/bash
# Uses a Hydra Influx DB dump created by 'dump-data.sh' to launch an Influx container with the
# Database dump loaded + a viewer Grafana container (port 81) for visualization + analysis

# Temp dirs used for expanding the DB dump to and for holding the volume data for the loader influx instance
export BACKUPDIR="$(pwd)/dump"
export INFLUXDIR="$(pwd)/influx-viewing"

# Clean up dirs and then expand the dump to the dump dir
rm -rf "$BACKUPDIR" || true
rm -rf "$INFLUXDIR" || true
mkdir "$BACKUPDIR"
tar -xzf *metrics*.tar.gz -C ./dump

# Rebuild DB in an ephemeral container with a bind mount to export results
# Roughly as suggested by https://www.influxdata.com/blog/backuprestore-of-influxdb-fromto-docker-containers/
docker run -d --rm --name influx-temp \
    -e ADMIN_USER="root" -e INFLUXDB_INIT_PWD="somepassword" \
    --entrypoint /bin/bash \
    -v $INFLUXDIR:/data \
    -v $BACKUPDIR:/dump \
    appcelerator/influxdb:influxdb-1.2.2 \
    -c "influxd restore -metadir /data/meta -datadir /data/db -database hydra /dump/hydra_influx_dump" 

# Launches dockerize influx using the bind-mount folder as the data source
docker run -d --rm -h influx --name influx --network scalability-bridge \
    -p 8083:8083 -p 8086:8086 -p 2015:2015 -e GRAPHITE_BINDING=':2015' -e GRAPHITE_PROTOCOL="tcp" \
    -e ADMIN_USER="root" -e INFLUXDB_INIT_PWD="somepassword" \
    -v $INFLUXDIR:/data \
    -e GRAPHITE_template="measurement*" appcelerator/influxdb:influxdb-1.2.2

# Launch separate container for graphana viewing
# Ports 81 - grafana
# TODO may need to set env var ROOT_BLKDEV_NAME to vda etc
docker run --rm -d --network scalability-bridge \
  -h grafana --name grafana \
  -p 81:3000 \
  temp-grafana:1.0
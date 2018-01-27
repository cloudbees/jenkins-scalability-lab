#!/bin/sh
# Run from the controlling docker server to dump the Hydra metrics DB from InfluxDB
docker exec influx influxd backup -database hydra /tmp/hydra_influx_dump
docker exec influx tar -C /tmp -cvzf /tmp/hydra-metrics.tar.gz hydra_influx_dump/
docker cp influx:/tmp/hydra-metrics.tar.gz hydra-metrics.tar.gze
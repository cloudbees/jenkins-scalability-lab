#!/bin/bash

# Launch grafana to speak to AWS
# You'll need to customize the datasource to use the AWS Influx IP
docker run --rm -d \
  -h grafana --name grafana \
  -p 81:3000 \
  temp-grafana:1.0
#!/bin/bash

# Launch grafana to speak to AWS
docker run --rm -d \
  -h grafana --name grafana \
  -p 81:3000 \
  temp-grafana:1.0
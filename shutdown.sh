#!/bin/bash
set -o
docker kill gitserver graphite jenkins || true
for i in $(docker ps -f label=role=agent -q); do
    docker kill $i || true
done
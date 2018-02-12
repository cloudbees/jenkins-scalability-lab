#!/bin/bash
# Runs full test
java -jar target/hydra-runner-1.0-SNAPSHOT-jar-with-dependencies.jar -t "legacy-pipeline,max-survivability" -f fulltest.txt -w 30000 -i localhost:8086 -j localhost:8080
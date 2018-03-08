#!/bin/bash
java -jar target/hydra-runner-1.0-SNAPSHOT.jar -t "basic-test,sample" -f basic-test.txt -w 30000 -i localhost:8086 -j localhost:8080
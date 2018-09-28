#!/bin/bash
set -xe
AGENT_NUMBER=${1:-5}

for ((n=0;n<${AGENT_NUMBER};n++)) ; do
    bash -ex launch-agent.sh $n
done

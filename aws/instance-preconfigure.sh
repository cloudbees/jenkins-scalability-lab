#!/usr/bin/env bash

set -xe
set -o pipefail

sudo yum install -y git
sudo yum install -y docker && sudo service docker start

#!/usr/bin/env bash

set -xe
set -o pipefail

sudo yum install -y git
sudo yum install -y docker && sudo service docker start
sudo chkconfig docker on
sudo groupadd docker || echo "Group already exists"
sudo usermod -aG docker $USER

# Add Maven installation?

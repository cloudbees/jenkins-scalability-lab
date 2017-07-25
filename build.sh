#!/bin/bash
set -xe
set -o pipefail

# TODO generate pubkey with ssh-keygen -t rsa and copy to gitserver and jenkins?
mkdir -p gitserver/keys || true
if [ ! -f id_rsa ]; then
  ssh-keygen -t rsa -n "" -P "" -f id_rsa
fi
cp id_rsa.pub gitserver/keys && cp id_rsa* jenkins
JENKINS_PUB_KEY=$(cat gitserver/keys/id_rsa.pub)

# Build jenkins image
if [[ "$(docker images -q jenkins-scalability-master:1.0 2> /dev/null)" == "" ]]; then
  docker build -t jenkins-scalability-master:1.0 ./jenkins
fi

# Start git server  https://github.com/jkarlosb/git-server-docker
# Hacks due to limitations on use of compose with docker IO resource limits, boo
LOCAL_DIR="$(pwd)"
docker run --rm -d -p 2222:22 \
   -h gitserver \
   --name gitserver -l role=gitserver \
   -v "$LOCAL_DIR/gitserver/keys:/git-server/keys" \
   -v "$LOCAL_DIR/gitserver/repos:/git-server/repos" \
   jkarlos/git-server-docker

# Start slave agent executors, names "agent-1, agent-2, etc"
AGENT_LIST=""
AGENT_LINKS=""
for (( c=1; c<=4; c++ )); do
    AGENT_NAME="agent-${c}"
    docker run -d --rm \
      -h "$AGENT_NAME" --name "$AGENT_NAME" -l role=agent --link gitserver \
      jenkinsci/ssh-slave "$JENKINS_PUB_KEY"
    AGENT_LIST="$AGENT_LIST $AGENT_NAME"
    AGENT_LINKS="$AGENT_LINKS --link $AGENT_NAME"
    # TODO create a string of all agent names
done

# Graphite server
docker run --rm -d \
  -h graphite --name graphite \
  -p 81:80 \
  -p 2003:2003 \
  -p 8125:8125/udp \
  hopsoft/graphite-statsd

# We need the root block device for resource limits, since the device name can change
ROOT_BLKDEV=/dev/$(docker run --rm -it jenkins-scalability-master:1.0 lsblk -d -o NAME | tail -n 1 | tr -d '\r' | tr -d '\n')


# Run jenkins
docker run -it --rm  -h jenkins --name jenkins -l role=jenkins \
  --device-write-iops $ROOT_BLKDEV:200 --device-write-bps $ROOT_BLKDEV:100mb --device-read-iops $ROOT_BLKDEV:200 --device-read-bps $ROOT_BLKDEV:100mb \
  -p 8080:8080 -p 9011:9011 \
  --link graphite \
  --link gitserver $AGENT_LINKS \
  jenkins-scalability-master:1.0
#  -v $(pwd)/jenkins/jenkins_home:/var/jenkins_home \
  
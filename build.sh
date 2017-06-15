# Start git server  https://github.com/jkarlosb/git-server-docker
# Hacks due to limitations on use of compose with docker IO resource limits, boo
LOCAL_DIR="$(pwd)"
docker run -d -p 2222:22 \
   -h gitserver
   -v "$LOCAL_DIR/gitserver/keys:/git-server/keys" \
   -v "$LOCAL_DIR/gitserver/:/git-server/repos" \
   jkarlos/git-server-docker

# TODO generate pubkey
JENKINS_PUB_KEY='nope'

# Start slave agent executors, names "agent-1, agent-2, etc"
for (( c=1; c<=4; c++ )); do
    docker run -h "agent-${c}" --link gitserver --rm jenkinsci/ssh-slave "$JENKINS_PUB_KEY"
    # TODO create a string of all agent names
done

docker run --link gitserver --rm jenkinsci/ssh-slave "<public key>"


# Build jenkins
docker build -t jenkins-scalability-master:1.0 ./jenkins

# Run jenkins
docker run -it --rm \
  --device-write-iops /dev/vda:200 --device-write-bps /dev/vda:100mb --device-read-iops /dev/vda:200 --device-read-bps /dev/vda:100mb \
  -p 8080:8080 -p 50000:50000 \
  -v /Users/svanoort/Documents/jenkins-scalability-lab/jenkinsjenkins_home:/var/jenkins_home \
  jenkinsci/jenkins:lts
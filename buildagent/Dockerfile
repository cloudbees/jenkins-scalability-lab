FROM vfarcic/jenkins-swarm-agent

# Copy in content needed for build agent
# Install JDK. Without installing this, jobs try to run with a 
# JRE, instead of a JDK. And that doesn't work at all.
RUN apk --update --no-cache add openjdk8
# Install Maven
RUN apk --update --no-cache add maven

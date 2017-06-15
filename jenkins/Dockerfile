FROM jenkinsci/jenkins:2.32.3.1
MAINTAINER svanoort

# Basic config
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
COPY executors.groovy /usr/share/jenkins/ref/init.groovy.d/executors.groovy

# TODO Provide some SSH keys
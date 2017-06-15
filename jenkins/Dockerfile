FROM jenkinsci/jenkins:2.32.3
MAINTAINER svanoort

# Workaround for bug in plugin.sh script where it tries to remove nonexistent lock
RUN mkdir -p /usr/share/jenkins/ref/plugins
RUN echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock

# Basic config
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt
COPY executors.groovy /usr/share/jenkins/ref/init.groovy.d/executors.groovy

# RSA key hack
COPY id_rsa /tmp/id_rsa
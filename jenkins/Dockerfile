FROM jenkinsci/jenkins:2.32.3
MAINTAINER svanoort

# Workaround for bug in plugin.sh script where it tries to remove nonexistent lock
RUN mkdir -p /usr/share/jenkins/ref/plugins
RUN echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock

# Basic config
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/ref/plugins.txt | tr '\n' ' ')
RUN rm -f /usr/share/jenkins/ref/init.groovy.d/executors.groovy
COPY loadtestsetup.groovy /usr/share/jenkins/ref/init.groovy.d/loadtestsetup.groovy

# prevent install wizard running, but tied to this version
COPY jenkins.* /usr/share/jenkins/ref/

# Below should work, but DOES NOT, setup wizard still shows up!!!
# ENV JENKINS_JAVA_OPTIONS "-Dhudson.Main.development=true -Djenkins.install.runSetupWizard=false -Djenkins.install.UpgradeWizard.show=false"

# RSA key hack
COPY id_rsa /tmp/id_rsa
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

# Below adds support for attaching a remote debugger
ENV JENKINS_JAVA_OPTIONS "-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9011 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1"
# Note that we can't disable the setup wizard with: -Dhudson.Main.development=true -Djenkins.install.runSetupWizard=false -Djenkins.install.UpgradeWizard.show=false

# RSA key hack, try to set it up so it doesn't use tmp in the near future
USER root
COPY id_rsa* /usr/share/jenkins/ref/
# Confirm this actually chowns, seems to not work
RUN chown -R jenkins:jenkins /usr/share/jenkins/ref/id_rsa* \
    && chmod 600 usr/share/jenkins/ref/id_rsa*
USER jenkins
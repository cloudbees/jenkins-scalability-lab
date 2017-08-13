FROM jenkinsci/jenkins:2.32.3
MAINTAINER svanoort

# Install telegraf for monitoring
USER root
RUN curl -o /tmp/telegraf.deb https://dl.influxdata.com/telegraf/releases/telegraf_1.3.5-1_amd64.deb \
    && dpkg -i /tmp/telegraf.deb && rm -f telegraf.deb
USER jenkins
COPY telegraf.conf /etc/telegraf/telegraf.conf
# TODO add autostart for telegraf to the jenkins startup script

# Workaround for bug in plugin.sh script where it tries to remove nonexistent lock
RUN mkdir -p /usr/share/jenkins/ref/plugins
RUN echo 'placeholder' > /usr/share/jenkins/ref/plugins/placeholder.lock

# Basic config - install plugins and customize the startup groovy script
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh $(cat /usr/share/jenkins/ref/plugins.txt | tr '\n' ' ')
COPY CUSTOM-PLUGINS/*.*pi /usr/share/jenkins/ref/plugins/

# Remove stale executor files and rename HPI files to JPI files to input custom versions
RUN rm -f /usr/share/jenkins/ref/init.groovy.d/executors.groovy \
    && cd /usr/share/jenkins/ref/plugins/ \
    && for custom_plugin in $(ls *.hpi); do mv "$custom_plugin" "${custom_plugin%.hpi}.jpi"; done
COPY loadtestsetup.groovy /usr/share/jenkins/ref/init.groovy.d/loadtestsetup.groovy

# prevent install wizard running, but tied to this version
COPY jenkins.* /usr/share/jenkins/ref/

# Below makes graphite metrics report more often and adds support for attaching a remote debugger
ENV JENKINS_JAVA_OPTIONS "-Dgraphite.metrics.intervalSeconds=5 -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9011 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1"
# Note that we can't disable the setup wizard with: -Dhudson.Main.development=true -Djenkins.install.runSetupWizard=false -Djenkins.install.UpgradeWizard.show=false

# RSA key hack, try to set it up so it doesn't use tmp in the near future
USER root
COPY id_rsa* /usr/share/jenkins/ref/
# Chown and chmod files copied in from externally
RUN chown -R jenkins:jenkins /usr/share/jenkins/ref/id_rsa* \
    && chmod 600 usr/share/jenkins/ref/id_rsa*
USER jenkins
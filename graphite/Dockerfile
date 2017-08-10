FROM alexmercer/graphite-grafana

# TODO Rebuild to use grafana 4
# https://github.com/kamon-io/docker-grafana-graphite
# https://github.com/m30m/docker-graphite-grafana - current
# https://github.com/grafana/grafana-docker/blob/master/Dockerfile
# https://thepracticalsysadmin.com/composing-a-graphite-server-with-docker/
# https://github.com/raintank/graphite-stack

ADD carbon.conf /var/lib/graphite/conf/carbon.conf
ADD storage-schemas.conf /var/lib/graphite/conf/storage-schemas.conf
ADD storage-aggregation.conf /var/lib/graphite/conf/storage-aggregation.conf


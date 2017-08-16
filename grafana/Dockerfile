FROM grafana/grafana:4.3.2
COPY grafana.ini /etc/grafana/grafana.ini

# Dashboards - note you need to export them via 'view JSON' to make this work right with datasources
# COPY dashboards/*.json /var/lib/grafana/dashboards/

# To enable preconfigured datasources and some of the dashboard stuff
COPY grafana.db /var/lib/grafana/grafana.db

# Will need running Grafana instance to run the below
# Needs a user set up?  See - https://github.com/grafana/grafana/issues/1789
# RUN curl -i -XPOST 'http://admin:admin@grafana:3000/api/datasources' --data-binary '{"name":"influxdbcontainer","type":"influxdb","access":"proxy","url":"http://influx:8086\",\"password\":\"somepassword\",\"user\":\"root\",\"database\":\"my_db\",\"basicAuth\":false,\"isDefault\":true,\"jsonData\":null}'
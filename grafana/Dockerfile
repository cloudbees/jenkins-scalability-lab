FROM grafana/grafana:4.3.2
COPY grafana.ini /etc/grafana/grafana.ini

# Will need running Grafana instance to run the below
# Needs a user set up?  See - https://github.com/grafana/grafana/issues/1789
# RUN curl -i -XPOST 'http://admin:admin@grafana:3000/api/datasources' --data-binary '{"name":"influxdbcontainer","type":"influxdb","access":"proxy","url":"http://influx:8086\",\"password\":\"somepassword\",\"user\":\"root\",\"database\":\"my_db\",\"basicAuth\":false,\"isDefault\":true,\"jsonData\":null}'
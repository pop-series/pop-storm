# deploy:

project for handling deployment of the storm cluster for ease of local development

## cluster setup:
1. cd deploy/
2. start: `docker-compose up --build -d`
3. stop: `docker-compose down`

## topology submission:
1. prepare the fat jar and flux yaml for the topology to be submitted
2. upload the above files to nimbus container using:
```
docker cp /path-to-dir/file-name pop-stormnimbus:/tmp/file-name
```
3. submit to cluster using:
```
docker exec --env-file deploy/admin/conf/dev.env -i pop-stormnimbus /opt/pop-storm/admin/scripts/submit.sh \
  -t=/container-path/your-topology.yaml -j=/container-path/your-app.jar
```

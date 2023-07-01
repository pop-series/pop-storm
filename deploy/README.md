# deploy:

project for automating spinning-up a new storm cluster for ease of emulating remote cluster while doing local development, esp. along with other pop-series projects.

## cluster setup:
1. cd deploy/
2. start: `docker-compose up --build -d`
3. stop: `docker-compose down`

other alternatives:
1. using `LocalCluster` either via Java TopologyBuilder or flux for most of the scenarios.
2. using storm image available in dockerhub (ref: https://github.com/31z4/storm-docker)

FROM java
MAINTAINER Bairen Yi <byi@connect.ust.hk>

RUN apt-get update && apt-get install -y gradle

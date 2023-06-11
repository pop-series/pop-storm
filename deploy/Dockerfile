# syntax=docker/dockerfile:1


FROM centos:7
RUN useradd -ms /bin/bash leafnode
WORKDIR /opt
RUN yum -y -q -e 1 install wget less vim nc lsof man gettext
RUN wget -q "https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz" && \
    tar xf "openjdk-11.0.2_linux-x64_bin.tar.gz" && \
    ln -s /opt/jdk-11.0.2 /opt/java11
RUN wget -q --no-check-certificate "https://dlcdn.apache.org/storm/apache-storm-2.4.0/apache-storm-2.4.0.tar.gz" && \
    tar xf apache-storm-2.4.0.tar.gz && \
    ln -s /opt/apache-storm-2.4.0 /opt/apache-storm
RUN mkdir -p /var/pop-storm && \
    chown -R leafnode:leafnode /var/pop-storm

USER leafnode
ENV USER=leafnode \
    TZ=Asia/Calcutta \
    JAVA_HOME=/opt/java11

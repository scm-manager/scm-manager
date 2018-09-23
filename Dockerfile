FROM maven:3.5.4-jdk-8 as builder
COPY . /usr/src

WORKDIR /usr/src
RUN mvn clean install

FROM openjdk:8u171-jdk-alpine3.8

ENV SCM_HOME /var/lib/scm

COPY --from=builder /usr/src/scm-server/target/scm-server-app.tar.gz /opt/scm-server-app.tar.gz
RUN set -x \
 && mkdir /var/lib/scm \
 && addgroup -g 1000 -S scm \
 && adduser -D -H -u 1000 -h /opt/scm-server -G scm scm \
 && tar xfz /opt/scm-server-app.tar.gz -C /opt \
 && chown -R scm:scm /opt/scm-server /var/lib/scm \
 && apk add --no-cache bash mercurial

EXPOSE 8080
USER scm
WORKDIR /opt/scm-server

ENTRYPOINT [ "/opt/scm-server/bin/scm-server" ]

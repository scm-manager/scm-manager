FROM openjdk:8u171-alpine3.8

ENV SCM_HOME=/var/lib/scm

RUN set -x \
 && apk add --no-cache mercurial bash \
 && addgroup -S -g 1000 scm \
 && adduser -S -s /bin/false -G scm -h /opt/scm-server -D -H -u 1000 scm \
 && mkdir ${SCM_HOME} \
 && chown scm:scm ${SCM_HOME}

ADD scm-server/target/scm-server-app.tar.gz /opt
RUN chown -R scm:scm /opt/scm-server

WORKDIR /opt/scm-server
VOLUME [ "${SCM_HOME}", "/opt/scm-server/var/log" ]
EXPOSE 8080
USER scm

ENTRYPOINT [ "/opt/scm-server/bin/scm-server" ]

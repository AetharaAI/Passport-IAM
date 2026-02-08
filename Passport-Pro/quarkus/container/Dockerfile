FROM registry.access.redhat.com/ubi9 AS ubi-micro-build

ARG PASSPORT_VERSION=999.0.0-SNAPSHOT
ARG PASSPORT_DIST=https://github.com/passport/passport/releases/download/$PASSPORT_VERSION/passport-$PASSPORT_VERSION.tar.gz

RUN dnf install -y tar gzip

ADD $PASSPORT_DIST /tmp/passport/

# The next step makes it uniform for local development and upstream built.
# If it is a local tar archive then it is unpacked, if from remote is just downloaded.
RUN (cd /tmp/passport && \
    tar -xvf /tmp/passport/passport-*.tar.gz && \
    rm /tmp/passport/passport-*.tar.gz) || true

RUN mv /tmp/passport/passport-* /opt/passport && mkdir -p /opt/passport/data
RUN chmod -R g+rwX /opt/passport

ADD ubi-null.sh /tmp/
RUN bash /tmp/ubi-null.sh java-21-openjdk-headless glibc-langpack-en findutils

FROM registry.access.redhat.com/ubi9-micro
ENV LANG=en_US.UTF-8

# Flag for determining app is running in container
ENV KC_RUN_IN_CONTAINER=true

COPY --from=ubi-micro-build /tmp/null/rootfs/ /
COPY --from=ubi-micro-build --chown=1000:0 /opt/passport /opt/passport

RUN echo "passport:x:0:root" >> /etc/group && \
    echo "passport:x:1000:0:passport user:/opt/passport:/sbin/nologin" >> /etc/passwd

USER 1000

EXPOSE 8080
EXPOSE 8443
EXPOSE 9000

ENTRYPOINT [ "/opt/passport/bin/kc.sh" ]

# common labels
ARG PASSPORT_VERSION
ARG PASSPORT_URL="https://www.passport-pro.ai/"
ARG PASSPORT_TAGS="passport security identity"
ARG PASSPORT_MAINTAINER=${PASSPORT_URL}
ARG PASSPORT_VENDOR=${PASSPORT_MAINTAINER}

LABEL maintainer=${PASSPORT_MAINTAINER} \
      vendor=${PASSPORT_VENDOR} \
      version=${PASSPORT_VERSION} \
      url=${PASSPORT_URL} \
      io.openshift.tags=${PASSPORT_TAGS} \
      release="" \
      vcs-ref="" \
      com.redhat.build-host="" \
      com.redhat.component="" \
      com.redhat.license_terms=""

# server specific
ARG PASSPORT_SERVER_DISPLAY_NAME="Passport Server"
ARG PASSPORT_SERVER_IMAGE_NAME="passport"
ARG PASSPORT_SERVER_DESCRIPTION="${PASSPORT_SERVER_DISPLAY_NAME} Image"

LABEL name=${PASSPORT_SERVER_IMAGE_NAME} \
      description=${PASSPORT_SERVER_DESCRIPTION} \
      summary=${PASSPORT_SERVER_DESCRIPTION} \
      io.k8s.display-name=${PASSPORT_SERVER_DISPLAY_NAME} \
      io.k8s.description=${PASSPORT_SERVER_DESCRIPTION}

# oci
ARG PASSPORT_SOURCE="https://github.com/passport/passport"
ARG PASSPORT_DOCS=${PASSPORT_URL}documentation

LABEL org.opencontainers.image.title=${PASSPORT_SERVER_DISPLAY_NAME} \
      org.opencontainers.image.url=${PASSPORT_URL} \
      org.opencontainers.image.source=${PASSPORT_SOURCE} \
      org.opencontainers.image.description=${PASSPORT_SERVER_DESCRIPTION} \
      org.opencontainers.image.documentation=${PASSPORT_DOCS}

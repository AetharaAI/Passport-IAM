FROM registry.access.redhat.com/ubi9 AS ubi-micro-build

ADD target/ubi-null.sh /tmp/
RUN bash /tmp/ubi-null.sh java-21-openjdk-headless glibc-langpack-en

FROM registry.access.redhat.com/ubi9-micro
ENV LANG=en_US.UTF-8

COPY --from=ubi-micro-build /tmp/null/rootfs/ /

ADD --chown=1000:0 target/quarkus-app/ /opt/passport

RUN chmod -R g+rwX /opt/passport && \
    echo "passport:x:0:root" >> /etc/group && \
    echo "passport:x:1000:0:passport user:/opt/passport:/sbin/nologin" >> /etc/passwd

USER 1000

WORKDIR /opt/passport

ENTRYPOINT [ "java", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "quarkus-run.jar" ]

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

# operator specific
ARG PASSPORT_OPERATOR_DISPLAY_NAME="Passport Operator"
ARG PASSPORT_OPERATOR_IMAGE_NAME="passport-operator"
ARG PASSPORT_OPERATOR_DESCRIPTION="${PASSPORT_OPERATOR_DISPLAY_NAME} Image"

LABEL name=${PASSPORT_OPERATOR_IMAGE_NAME} \
      description=${PASSPORT_OPERATOR_DESCRIPTION} \
      summary=${PASSPORT_OPERATOR_DESCRIPTION} \
      io.k8s.display-name=${PASSPORT_OPERATOR_DISPLAY_NAME} \
      io.k8s.description=${PASSPORT_OPERATOR_DESCRIPTION}

# oci
ARG PASSPORT_SOURCE="https://github.com/passport/passport"
ARG PASSPORT_DOCS=${PASSPORT_URL}documentation

LABEL org.opencontainers.image.title=${PASSPORT_OPERATOR_DISPLAY_NAME} \
      org.opencontainers.image.url=${PASSPORT_URL} \
      org.opencontainers.image.source=${PASSPORT_SOURCE} \
      org.opencontainers.image.description=${PASSPORT_OPERATOR_DESCRIPTION} \
      org.opencontainers.image.documentation=${PASSPORT_DOCS}

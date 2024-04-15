ARG VERSION=1.0.0

FROM maven:3.9-eclipse-temurin-21 AS builder

COPY . /tmp/ 

WORKDIR /tmp/

RUN --mount=type=secret,id=MAVEN_SETTINGS mvn clean package -s /run/secrets/MAVEN_SETTINGS

FROM amazoncorretto:22

ARG VERSION

RUN yum install -y procps python3 python3-pip

ENV PIPX_BIN_DIR=/opt/bin/

RUN pip3 install pipx && pipx install crypt4gh && pipx ensurepath

COPY --from=builder /tmp/target/globus-file-handler-cli-${VERSION}.jar /opt/


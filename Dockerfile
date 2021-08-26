FROM maven:alpine

RUN apk add gnupg
ENV ENV "test"
WORKDIR /home
ADD . /home

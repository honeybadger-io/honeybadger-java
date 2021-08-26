FROM maven:alpine

ENV ENV "test"
WORKDIR /home
ADD . /home

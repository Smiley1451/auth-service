FROM ubuntu:latest
LABEL authors="smile"

ENTRYPOINT ["top", "-b"]
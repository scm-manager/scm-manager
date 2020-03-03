FROM adoptopenjdk/openjdk11:x86_64-debian-jdk-11.0.6_10

# install required packages
RUN apt-get update \
 && apt-get install -y \
    # mercurial is requried for integration tests of the scm-hg-plugin
    mercurial \
    # git is required by yarn install of scm-ui
    git \
 # clear apt caching
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

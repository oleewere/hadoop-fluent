FROM fluent/fluentd:v1.4.0-debian-1.0

MAINTAINER Oliver Szabo <oleewere@gmail.com>

EXPOSE 24284

RUN mkdir -p /fluentd/etc /fluentd/plugins

USER root

ENV APPS_TO_INSTALL make gcc g++ libc-dev ruby-dev zlib1g-dev libz-dev git
ENV FLUENTD_PLUGINS_TO_INSTALL fluent-plugin-concat fluent-plugin-multiline-parser fluent-plugin-webhdfs fluent-plugin-s3 fluent-plugin-azurestorage fluent-plugin-gcs fluent-plugin-cloudwatch-logs fluent-plugin-multi-format-parser

RUN apt-get update && apt-get install -y --no-install-recommends $APPS_TO_INSTALL
RUN gem install $FLUENTD_PLUGINS_TO_INSTALL
RUN gem install http-2 aws-sdk-cloudwatchlogs
RUN gem sources --clear-all
RUN apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false $APPS_TO_INSTALL && apt-get clean && rm -rf /var/lib/apt/lists/*
RUN rm -rf /home/fluent/.gem/ruby/*/cache/*.gem && rm -rf /var/lib/gems/*/cache/*.gem

# Java
RUN mkdir -p /usr/share/man/man1
RUN apt-get update && apt-get install -y gnupg
RUN apt install -y default-jdk

ADD build/hadoop-cloud-clients /usr/lib/hadoop-cloud-clients

WORKDIR  /home/fluent/

CMD ["fluentd", "-c", "/fluentd/etc/fluent.conf"]
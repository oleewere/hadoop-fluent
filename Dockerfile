FROM openjdk:8
ADD ./build/hadoop-fluent /root/hadoop-fluent
RUN chmod +x /root/hadoop-fluent/bin/hadoop-fluent.sh

WORKDIR /root/hadoop-fluent

ENTRYPOINT ["bin/hadoop-fluent.sh"]

FROM openjdk:8-jdk-slim
VOLUME /tmp
COPY ./thread-tx.jar /home/tempura/thread-tx.jar
COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas
EXPOSE 7001
RUN bash -c 'touch thread-tx.jar'
WORKDIR /home/tempura
ENV TZ 'Asia/Shanghai'
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
CMD ["java", "-jar", "-Xmx2g", "-Xms2g", "-Duser.timezone=Asia/Shanghai", "-Dserver.port=7001", "-server", "/home/tempura/thread-tx.jar"]

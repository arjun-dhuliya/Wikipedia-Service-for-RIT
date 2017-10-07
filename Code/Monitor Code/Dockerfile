FROM java:8
COPY Handlers.java /
COPY SimpleHttpServer.java /
COPY Config.java /
COPY mycert.keystore /
COPY Monitor.java /
RUN javac *.java
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["Monitor"]
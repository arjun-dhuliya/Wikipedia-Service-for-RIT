FROM java:8
COPY Handlers.java /
COPY SimpleHttpServer.java /
COPY mongo-java-driver-3.4.2.jar /
COPY Config.java /
COPY mycert.keystore /
COPY WorkerNode.java /
RUN javac -cp ".:/mongo-java-driver-3.4.2.jar" *.java
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-cp",".:/mongo-java-driver-3.4.2.jar","WorkerNode"]
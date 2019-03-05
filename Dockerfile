FROM maven:3-jdk-8

COPY . /home
WORKDIR /home

RUN mkdir Desktop

RUN mkdir Desktop/cpswarm

RUN mkdir Desktop/optimized

RUN mkdir Desktop/conf

RUN mvn -B validate

RUN mvn install -DskipTests

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64/



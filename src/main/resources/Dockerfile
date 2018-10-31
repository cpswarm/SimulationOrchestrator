FROM maven:3-jdk-8

COPY . /home
WORKDIR /home

RUN mkdir Desktop

RUN mkdir Desktop/cpswarm



RUN mvn -B validate

RUN mvn install -DskipTests

# install xvfb to create a virtual display for tests
# RUN apt update && apt install -y xvfb

ENV JAVA_HOME /usr/lib/jvm/java-1.8.0-openjdk-amd64/

RUN keytool -noprompt -importcert -trustcacerts \
    -file pert-demoenergy-virtus.ismb.polito.it.pem -alias pert-demoenergy-virtus.ismb.polito.it \
    -storepass changeit -keystore -J-Duser.language=en $JAVA_HOME/jre/lib/security/cacerts
   
# create a virtual display with Xvfb and set DISPLAY before starting java
# RUN Xvfb :1 -screen 0 1024x768x16 & DISPLAY=:1.0 \

CMD java -jar /home/target/it.ismb.pert.cpswarm.simulation.orchestrator-1.0.0-jar-with-dependencies.jar


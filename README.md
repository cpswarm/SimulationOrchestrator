#Simulation and Optimization Orchestrator

This is the project for the Simulation and Optimization Orchestrator.

## Setup

The project is a maven project, based on Java 8. To compile it you need to have (Java 8 JDK)[http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html] and (maven)[https://maven.apache.org/].

## Installation

The project can be installed with the following command

``` bash
mvn install 
```

The software contains some tests, which require a setup, if you want to avoid them

``` bash
mvn install -DskitTests 
```

#Configuration
The software contains a configuration file which can be used to change some system parameter, this file is /resources/orchestator.xml

``` xml
<settings>
	<serverURI>130.192.86.237</serverURI> <!--  URI of the XMPP server  -->
	<serverName>pert-demoenergy-virtus.ismb.polito.it</serverName> <!--  name of the XMPP server  -->
	<serverPassword>orchestrator</serverPassword> <!--  password to be used from the orchestator to connect to the XMPP server  -->
	<optimizationUser>frevo</optimizationUser> <!--  XMPP username of the Optimization Tool  --> 
	<monitoring>true</monitoring> <!--  indication if the monitoring GUI has to be used or not  -->
	<mqttBroker>tcp://130.192.86.237:1883</mqttBroker> <!--  MQTT broker to be used if the monitoring is set to true  -->
</settings>

```


## Run

These are the parameters to be passed to the software to run

``` bash
required options: s, t, c, i
usage: utility-name
 -c,--conf <arg>     folder with the configuration files
 -g,--gui <arg>      GUI to be used or not for the simulation
 -i,--id <arg>       optimization ID
 -s,--src <arg>      input folder path
 -t,--target <arg>   output folder path
```

And this is an example of running command

``` bash
java -jar /home/cpswarm/SimulationOrchestrator/target/it.ismb.pert.cpswarm.simulation.orchestrator-1.0.0-jar-with-dependencies.jar --id emergency_exit --src /home/cpswarm/launcher_project/Models --target /home/cpswarm/launcher_project/Optimized --conf /home/cpswarm/launcher_project/SimulationConf
```

## Test configuration

This is the command to be used to launch tests

``` bash
mvn test -Dtest_server_ip=130.192.86.237 -Dtest_server_name=pert-demoenergy-virtus.ismb.polito.it -Dtest_server_password=orchestrator -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ -Dtest_manager2_data_folder=/home/cpswarm/Desktop/output2/ -Doptimization_user=optimization_test -Dot_data_folder=/home/cpswarm/Desktop/ot/ -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ -Dros2_folder=/home/cpswarm/Desktop/test2/src/emergency_exit/src/ -Dmonitoring=true -Dmqtt_broker=tcp://130.192.86.237:1883 -Dgui_enabled=false -Doptimization_id=emergency_exit -Djavax.xml.accessExternalDTD=all
```

And here with the explaination of the parameters:

``` bash
	 -Dtest_server_ip=130.192.86.237 (IP of the XMPP server) 
	 -Dtest_server_name=pert-demoenergy-virtus.ismb.polito.it (name of the XMPP server) 
	 -Dtest_server_password=orchestrator (Password to be used by the orchestrator to authenticate in the XMPP server)
	 -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files)
	 -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted)
	 -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) 
	 -Dtest_manager2_data_folder=/home/cpswarm/Desktop/output2/ (data folder used by the second simulation manager for multiple simulations test)
	 -Doptimization_user=optimization_test (XMPP username used for the Optimization Tool)
	 -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool)
	 -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation)
	 -Dros2_folder=/home/cpswarm/Desktop/test2/src/emergency_exit/src/ (Folder used for the ROS package to start the second simulation for multiple simulations test)
	 -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
	 -Dmqtt_broker=tcp://130.192.86.237:1883  (IP of the MQTT broker to be used for the monitoring)
	 -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 -Doptimization_id=emergency_exit (ID of the optimization AKA the name of the package)
	 -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
```

Please, consider that to run the tests, you need to have all the folders indicated in the parameters already created.
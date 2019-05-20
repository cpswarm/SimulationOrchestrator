# Simulation and Optimization Orchestrator

This is the project for the Simulation and Optimization Orchestrator (SOO).

## Setup

The project is a maven project, based on Java 8. To compile it you need to have [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) and [maven](https://maven.apache.org/).

## Installation

The project can be installed with the following command

``` bash
mvn install 
```

The software contains some tests, which require a setup, if you want to avoid them

``` bash
mvn install -DskitTests 
```

# Configuration
The software contains a configuration file, which can be used to change some system parameter, this file is /resources/orchestator.xml

The project contains already a configuration file with default values, before to change it setting the values to be used in the real use case, remember to run this commands in the project folder

```
git update-index --skip-worktree src/main/resources/orchestrator.xml
git update-index --skip-worktree src/main/resources/frevoConfiguration.json
```


These are the values in the file

``` xml
<settings>
   <serverURI>123.123.123.123</serverURI> <!-- URI of the XMPP server  -->
	<serverName>pippo.pluto.it</serverName>  <!-- name of the XMPP server  -->
	<username>orchestrator</username> <!-- username to be used to connect to the XMPP Server -->
	<serverPassword>orchestrator</serverPassword> <!--  password to be used from the orchestator to connect to the XMPP server (temporary solution) -->
	<optimizationUser>frevo</optimizationUser> <!--  XMPP username of the Optimization Tool  --> 
	<monitoring>false</monitoring> <!--  indication if the monitoring GUI has to be used or not  -->
	<configEnabled>true</configEnabled> <!-- Indication if the configuration of the simulators has to be done or not -->
	<startingTimeout>30000</startingTimeout> <!-- Time used to wait new Simulation Managers -->
	<mqttBroker>tcp://123.123.123.123:1883</mqttBroker> <!--  MQTT broker to be used if the monitoring is set to true  -->
	<localOptimization>false</localOptimization> <!-- Indicates if the  Optimization Tool has to be launched by the Orchestrator -->
	<optimizationToolPath>/home/Desktop/frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar</optimizationToolPath> <!-- Path of the optimization tool executable -->
	<optimizationToolPassword>blah</optimizationToolPassword> <!-- To be used if the Optimization Tool has to be launched from the Orchestrator  -->
	<localSimulationManager>false</localSimulationManager> <!-- Indicates if the  Simulation Manager has to be launched by the Orchestrator -->
	<simulationManagerPath>/home/Desktop/frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar</SimulationManagerPath> <!-- Path of the simulation manager executable -->

</settings>
```


## Run

These are the parameters to be passed to the software to run

``` bash
usage: java -jar soo.jar
 -c,--conf <arg>     folder with the configuration files
 -cc,--can <arg>     Indicates the candidate count
 -d,--dim <arg>      Number of dimensions required for simulation
 -g,--gui            GUI to be used or not for the simulation
 -gc,--gen <arg>     Indicates the generation count
 -i,--id <arg>       Task ID
 -M,--mode <arg>     Running mode for the SOO [d, r, rd]
 -m,--max <arg>      Maximum number of agents required for simulation
 -o,--opt            Indicates if the optimization is required or not
 -p,--params <arg>   Parameters to be passed to the simulator
 -s,--src <arg>      input folder path
 -se,--seed <arg>    Indicates the seed to be used in the OT
 -st,--sim <arg>     Indicates the the simulation timeout for the OT
 -t,--target <arg>   output folder path
 ```

And this is an example of running command

``` bash
java -jar -M rd /home/cpswarm/SimulationOrchestrator/target/it.ismb.pert.cpswarm.simulation.orchestrator-1.0.0-jar-with-dependencies.jar --id emergency_exit --dim any --max 3 --src /home/cpswarm/launcher_project/Models --target /home/cpswarm/launcher_project/Optimized --conf /home/cpswarm/launcher_project/SimulationConf --opt --gui
```

## Test suite

### Test creation

This test is used to verify:
 - The creation of the XMPP client of the SOO on the XMPP server 
 - The creation of the XMPP client of the Dummy Simulation Manager used for test 
 - The creation of the rosters of the two components used to receive the presences
 - The test verifies that the manager has been successfully added to the roster of the SOO after been created

### Test run simulation

This test is used to verify:
 - The start of the SOO with a set of requirements for the simulation to be run (dimensions, number of agents)
 - The ability to match the requirements with the features provided by the Dummy Simulation Manager
 - The ability to select the Simulation Manager and to send the Run Simulation message
 - The test verifies that the Dummy Manager receives the correct XMPP message

### Test run optimization

This test is used to verify:
 - The start of the SOO with a set of requirements for the simulation to be run (dimensions, number of agents) and the request to do optimization.
 - The ability to match the requirements with the features provided by the Dummy Simulation Manager
 - The ability to send a Start Optimization message to the Dummy Optimization Tool
 - The ability to receive correctly the result of the optimization, when it is finished
 - The test verifies that the optimization is correctly finished

### Test kubernetes

This test is used to verify:
 - The possibility to use the Java client to connect to a Kubernetes Master.

## Test configuration

This is the command to be used to launch tests

``` bash
mvn test -Dtest_server_ip=123.123.123.123 -Dtest_server_name=pippo.pluto.it -Dtest_server_password=orchestrator -Dtest_orchestrator_output_data_folder= -Dtest_manager_data_folder= -Doptimization_user=optimization_test -Dot_data_folder= -Dros_folder=   -Dtask_id=cpswarm_sar -Dparameters="" -Dgui=false -Dmonitoring=false -Ddimensions="Any" -Dmax_agents=3 -Dlocal_optimzation=false -Dstarting_timeout=5000 -Djavax.xml.accessExternalDTD=all
```

And here with the explanation of the parameters:

``` bash
  -Dtest_server_ip=123.123.123.123 (IP of the XMPP server) 
  -Dtest_server_name=pippo.pluto.it (name of the XMPP server) 
  -Dtest_server_password=server (Password to be used by the SOO to authenticate in the XMPP server - temporary solution)
  -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files) - optional - it can be empty for test
  -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted) - optional - it can be empty for test
  -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) - optional - it can be empty for test
  -Doptimization_user=optimization_test (User of the Optimization Tool)
  -Dorchestrator_user=orchestrator User of the Simulation Orchestrator
  -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool, if run by the SOO) - optional - it can be empty for test
  -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation) - optional - it can be empty for test
  -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
  -Dmqtt_broker=tcp://123.123.123.123:1883  (IP of the MQTT broker to be used for the monitoring)
  -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
  -Dtask_id=emergency_exit (ID of the task AKA the name of the package)
  -Dparameters="" (indicates the parameters to be used in the simulations)
  -Ddimensions = "2D" (indicates the number of dimensions required for the simulation)
  -Dmax_agents="8" (indicates the maximum number of agents required for the simulation)
  -Dconfiguration_folder=/home/cpswarm/Desktop/conf/ (folder with the configuration files)
  -Dlocal_optimzation=false (Indicates if the Optimization Tool has to be launched by the SOO)
  -Doptimization_tool_path=/home/Desktop/frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar (path of the Optimization Tool, used if local_optimization = true)
  -Doptimization_tool_password = blah (password to be used to launch the optimization tool from the SOO, if local_optimization = true)
  
  -Dstarting_timeout=5000 (time to wait for the subscription of new Simulation Managers)
  -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
```


### Preliminary steps

This tutorial assumes that:
- You have installed on your machine or in the cloud one instance of an XMPP server. This software has been tested with [Openfire](https://www.igniterealtime.org/projects/openfire/) and [Tigase](https://tigase.net/). For a complete list of available XMPP servers see [here](https://xmpp.org/software/servers.html). 
- You have docker running on your machine.
- For the deployment of the Simulators, you have access to a [Kubernetes](https://kubernetes.io/) cluster and you have setup the KUBECONFIG environment variable to the path of a valid Kubernetes config file).
- The SOO is launched using [CPSwarm Launcher](https://github.com/cpswarm/launcher)

### Steps

Start cloning the project in your environment from the CPSwarm [github repository](https://github.com/cpswarm/SimulationOrchestrator.git).


Then you have to remove the configuration files from the git index, with the following commands.

``` bash
git update-index --skip-worktree src/main/resources/orchestrator.xml
git update-index --skip-worktree src/main/resources/frevoConfiguration.json
```

Then you can open the src/main/resources/orchestrator.xml file and configure it, setting your values:

``` xml
  <serverURI>123.123.123.123</serverURI> 
```
Use this tag to set the IP/URI of the XMPP server that you want to use.

``` xml
  <serverName>pippo.pluto.it</serverName> 
```
Use this tag to set the name of the XMPP server that you want to use.

``` xml
  <username>orchestrator</username> 
```
Use this tag to set username to be used to connect to the XMPP Server

``` xml
  <serverPassword>server</serverPassword> 
```
Use this tag to set the password to be used by the manager to connect (it is a temporary solution).

``` xml
  	<optimizationUser>frevo</optimizationUser>   
```
Use this tag to set the JID used by the Optimization Tool.
 
``` xml
  <monitoring>true</monitoring> 
```
Use this tag to set enable/disable the monitoring of the optimization process, done through MQTT. If it set to true, the manager will publish a new event, each time the one simulation is concluded.

``` xml
  <mqttBroker>tcp://123.123.123.123:1883</mqttBroker> 
```
Use this tag to set MQTT broker leveraged to publish the monitoring events if monitoring is set to true. 

``` xml
  <configEnabled>true</configEnabled> 
```
Use this tag to indicate if the configuration of the simulators has to be done or not. 

``` xml
  <startingTimeout>30000</startingTimeout> 
```
Use this tag to indicate the waiting time for new Simulation Managers. 

``` xml
  <localOptimization>false</localOptimization> 
```
Use this tag to indicate if the Optimization Tool has to be launched by the SOO (true) or not (false). 

``` xml
  <optimizationToolPath>/home/Desktop/frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar</optimizationToolPath> 
```
Use this tag to indicate the path of the Optimization Tool executable, to launch it if LocalOptimization=true. 

``` xml
  <optimizationToolPassword>blah</optimizationToolPassword> 
```
Use this tag to indicate the password of the Optimization Tool, needed if LocalOptimization=true (it is a temporary solution). 

Then, you can proceed to modify the Optimization Tool configuration file (if needed), open the file Then you can open the src/main/resources/frevoConfiguration.json file and configure it. Particularly, here you have to configure:

``` json
 simulationTimeoutSeconds 
```
Timeout after which the simulation is considered as failed. 

``` json
 generationCount
```
Number of generations used for the optimization.

``` json
 candidateCount 
```
Number of candidates tested for each generation during the optimization.

Now that you have configured it, you can create the docker instance, running this command:

``` bash
docker build . --tag soo 
```

Then you have to create the docker container to run the SOO, wiyh also the certificate to be used (for an example of this bundle, see: soo-runner):

``` bash
FROM soo:latest 
```

In the Docker file, set the command to run as

```
ENTRYPOINT ["java", "-jar", "/home/target/it.ismb.pert.cpswarm.simulation.orchestrator-1.0.0-jar-with-dependencies.jar"]
CMD ["--id", "emergency_exit", "--dim", "2d", "--max", "8", "--opt", "--src", "/home/Desktop/cpswarm/", "--target", "/home/Desktop/cpswarm/optimized/", "--conf", "/home/Desktop/conf/"]
```

In this way, it will be possible to pass to the SOO all the required parameters

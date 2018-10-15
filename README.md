# Simulation and Optimization Orchestrator

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

# Configuration
The software contains a configuration file which can be used to change some system parameter, this file is /resources/orchestator.xml

``` xml
<settings>
	<serverURI>130.192.86.237</serverURI> <!--  URI of the XMPP server  -->
	<serverName>pert-demoenergy-virtus.ismb.polito.it</serverName> <!--  name of the XMPP server  -->
	<serverPassword>orchestrator</serverPassword> <!--  password to be used from the orchestator to connect to the XMPP server  -->
	<optimizationUser>frevo</optimizationUser> <!--  XMPP username of the Optimization Tool  --> 
	<monitoring>true</monitoring> <!--  indication if the monitoring GUI has to be used or not  -->
	<mqttBroker>tcp://130.192.86.237:1883</mqttBroker> <!--  MQTT broker to be used if the monitoring is set to true  -->
		<localOptimization>true</localOptimization> <!-- Indicates if the  Optimization Tool has to be launched by the Orchestrator -->
	<optimizationToolPath>C:\Users\co_da\OneDrive\Desktop\xmpp\frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar</optimizationToolPath> <!-- Path of the optimization tool executable -->
	<optimizationToolPassword>blah</optimizationToolPassword> <!-- To be used if the Optimization Tool has to be launched from the Orchestrator  -->
</settings>

```


## Run

These are the parameters to be passed to the software to run

``` bash
required options: s, t, c, i, d, m, o
usage: utility-name
 -c,--conf <arg>     folder with the configuration files
 -d,--dim <arg>      Number of dimensions required for simulation
 -g,--gui <arg>      GUI to be used or not for the simulation
 -i,--id <arg>       task ID
 -m,--max <arg>      Maximum number of agents required for simulation
 -p,--params <arg>   Parameters to be passed to the simulator
 -s,--src <arg>      input folder path
 -t,--target <arg>   output folder path
 -o --opt <arg>      indicates if the optimization is enabled
 ```

And this is an example of running command

``` bash
java -jar /home/cpswarm/SimulationOrchestrator/target/it.ismb.pert.cpswarm.simulation.orchestrator-1.0.0-jar-with-dependencies.jar --id emergency_exit --dim any --max 3 --src "/home/cpswarm/launcher_project/Models" --target "/home/cpswarm/launcher_project/Optimized" --conf "/home/cpswarm/launcher_project/SimulationConf" --opt --gui
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
 - The start of the SOO with a set of requirements for the the simulation to be run (dimensions, number of agents)
 - The ability to match the requirements with the features provided by the Dummy Simulation Manager
 - The ability to select the Simulation Manager and to send the Run Simulation message
 - The test verifies that the Dummy Manager receives the correct XMPP message

### Test run optimization

This test is used to verify:
 - The start of the SOO with a set of requirements for the the simulation to be run (dimensions, number of agents) and the request to do optimization.
 - The ability to match the requirements with the features provided by the Dummy Simulation Manager
 - The ability to send a Start Optimization message to the Dummy Optimization Tool
 - The ability to receive correctly the result of the optimization, when it is finished
 - The test verifies that the optimization is correctly finished

### Test configuration

This is the command to be used to launch tests

``` bash
Dtest_server_ip=130.192.86.237 -Dtest_server_name=pert-demoenergy-virtus.ismb.polito.it -Dtest_server_password=orchestrator  -Dtest_orchestrator_output_data_folder= -Dtest_manager_data_folder= -Doptimization_user=optimization_test -Dot_data_folder= -Dros_folder=   -Dtask_id=cpswarm_sar -Dparameters="" -Dgui=false -Dmonitoring=false -Ddimensions="Any" -Dmax_agents=3  -Dlocal_optimzation=false  -Djavax.xml.accessExternalDTD=all
```

And here with the explaination of the parameters:

``` bash
	 -Dtest_server_ip=130.192.86.237 (IP of the XMPP server) 
	 -Dtest_server_name=pert-demoenergy-virtus.ismb.polito.it (name of the XMPP server) 
	 -Dtest_server_password=orchestrator (Password to be used by the orchestrator to authenticate in the XMPP server)
	 -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files) - optional - it can be empty for test
	 -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted) - optional - it can be empty for test
	 -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) - optional - it can be empty for test
	 -Doptimization_user=optimization_test (XMPP username used for the Optimization Tool)
	 -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool) - optional - it can be empty for test
	 -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation) - optional - it can be empty for test
	 -Dmonitoring=true (indicates if the monitoring GUI has to be used, monitoring the evolution of the optimization)
	 -Dmqtt_broker=tcp://130.192.86.237:1883  (IP of the MQTT broker to be used for the monitoring)
	 -Dgui_enabled=false (indicates if the GUI has to be used during the simulations)
	 -Dtask_id=emergency_exit (ID of the task AKA the name of the package)
	 -Dparameters="" (indicates the parameters to be used in the simulations)
	 -Ddimensions = "2D" (indicates the number of dimensions required for the simulation)
	 -DmaxAgents = "8" (indicates the maximum number of agents required for the simulation
	 -Dconfiguration_folder=/home/cpswarm/Desktop/conf/       folder with the configuration files
	 -Dlocal_optimzation=false Indicates if the Optimization Tool has to be launched by the Orchestrator
	-Doptimization_tool_path=C:\Users\co_da\OneDrive\Desktop\xmpp\frevo.xmpp-0.0.1-SNAPSHOT-jar-with-dependencies.jar (if local_optimization = true)
	-Doptimization_tool_password = blah Password to be used to launch the optimization tool from the orchestrator (if local_optimization = true)
	 -Djavax.xml.accessExternalDTD=all (configuration for xml parsing)
```

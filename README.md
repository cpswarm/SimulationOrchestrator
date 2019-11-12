# CPSwarm Simulation and Optimization Orchestrator

This is the project for the Simulation and Optimization Orchestrator (SOO). This component orchestrates the simulation and optimization process. 
It is the only interface between the Simulation and Optimization Environment and the rest of the workbench. The SOO is the centralized component connected, 
from one side to the Launcher and from the other side to the Optimization Tool (OT) and the distributed Simulation Managers (SM)s 
using the eXtensible Messaging and Presence Protocol (XMPP) protocol 


## Getting Started
* Documentation: [wiki](https://github.com/cpswarm/SimulationOrchestrator/wiki)

## Deployment
Packages are built continuously with [Bamboo](https://pipelines.linksmart.eu/browse/CPSW-SOO/latest).


### Compile from source
Within the root of the repository:

```bash
mvn install
```

The software contains some tests, which require a setup, if you want to avoid them

``` bash
mvn install -DskipTests 
```


## Development
### Run tests
This is the command to be used to launch tests

``` bash
mvn test -Dtest_server_ip=123.123.123.123 -Dtest_server_name=pippo.pluto.it -Dtest_server_password=orchestrator -Dtest_orchestrator_output_data_folder= -Dtest_manager_data_folder= -Doptimization_user=optimization_test -Dot_data_folder= -Dros_folder=   -Dtask_id=emergency_exit  -Dparameters="" -Dgui=false -Dmonitoring=false -Ddimensions="Any" -Dmax_agents=3 -Dlocal_optimzation=false -Dstarting_timeout=5000 -Djavax.xml.accessExternalDTD=all
```

And here with the explanation of the parameters:

``` bash
  -Dtest_server_ip=123.123.123.123 (IP of the XMPP server) 
  -Dtest_server_name=pippo.pluto.it (name of the XMPP server) 
  -Dtest_server_password=orchestrator (Password to be used by the SOO to authenticate in the XMPP server - temporary solution)
  -Dtest_orchestrator_input_data_folder=/home/cpswarm/Desktop/cpswarm/ (folder containing the input files) - optional - it can be empty for test
  -Dtest_orchestrator_output_data_folder=/home/cpswarm/Desktop/cpswarm-out (folder where the output files will be inserted) - optional - it can be empty for test
  -Dtest_manager_data_folder=/home/cpswarm/Desktop/output/ (data folder used by the simulation manager) - optional - it can be empty for test
  -Doptimization_user=optimization_test (User of the Optimization Tool)
  -Dorchestrator_user=orchestrator (User of the Simulation Orchestrator)
  -Dot_data_folder=/home/cpswarm/Desktop/ot/  (folder used by the Optimization Tool, if run by the SOO) - optional - it can be empty for test
  -Dros_folder=/home/cpswarm/Desktop/test/src/emergency_exit/src/ (Folder used for the ROS package to start the first simulation) - optional - it can be empty for test
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


### Dependencies
* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [maven](https://maven.apache.org/)
* XMPP Server
  * [Openfire](https://www.igniterealtime.org/projects/openfire/)
  * [Tigase](https://tigase.net/content/tigase-xmpp-server)

## Contributing
Contributions are welcome. 

Please fork, make your changes, and submit a pull request. For major changes, please open an issue first and discuss it with the other authors.

## Affiliation
![CPSwarm](https://github.com/cpswarm/template/raw/master/cpswarm.png)  
This work is supported by the European Commission through the [CPSwarm H2020 project](https://cpswarm.eu) under grant no. 731946.

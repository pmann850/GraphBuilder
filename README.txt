GRAPH BUILDER

Graph Builder is a currently a rudimentary application for building a graph database from various source files. The implementation works specifically for certain data sets but can be modified to parse for any number of sources and build graphs based on established design patterns.

BUILD INSTRUCTIONS

In order to run this you will need the following installed on your system.
Java
Ant
NetBeans (Optional)

You can build using ANT or Netbeans. The following scripts have been created to make building easier for you.
clean.bat - Removes build and dist directories
build.bat - Builds the application

RUN INSTRUCTIONS

Modify config.properties to set the database connectivity and input directory information.

Run by calling 
java -jar dist/GraphBuilder.jar
Or by executing the run.bat file.

KNOWN LIMITATIONS

This is a work in progress. This requires specific inputs and produces specific outputs. With more time I would make this more configurable.

FUTURE ENHANCEMENTS

Abstract parsers to allow for a plug-in architecture
Establish graph building patterns that can be driven by configuration
Drive parsing and graph building from a configuration file


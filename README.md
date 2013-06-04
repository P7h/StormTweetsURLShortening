# StormTweetsURLUnShortening

## Introduction
Forked and updated from [Michael Vogiatzis's storm-unshortening](https://github.com/mvogiatzis/storm-unshortening).<br>
This repository contains an application which is built to demonstrate an example of Storm distributed framework.

[Storm](http://storm-project.net) was developed at BackType by Nathan Marz and team. It has been open sourced by Twitter [post Backtype acquisition] in August, 2011.<br>
This application has been developed and tested with Storm v0.8.2 on CentOS. Application may or may not work with earlier or later versions than Storm v0.8.2.<br>

It has been tested in:

* Local mode on a CentOS virtual machine and even on Microsoft Windows 7 box.
* Cluster mode on a private cluster and also on Amazon EC2 environment of 4 machines and 5 machines respectively; with all of them running CentOS.

## Features
* Application retrieves tweets from Twitter stream (using [Twitter4J](http://twitter4j.org)) and in real-time unshortens the urls found in the tweets using 3rd party calls from [Unshort.me](http://unshort.me).<br>
* It logs the short and its resolved url pairs to the console and also to a log file.<br>
* It can also write the short-resolved url pairs into a Cassandra table using [CassandraBolt](src/main/java/org/p7h/storm/urlshorten/bolts/CassandraBolt.java); currenly this functionality is commented out.<br>
* I have plans to update this codebase in few weeks time for storing the analyzed data to HBase as well using a HBase Bolt.
* As of today, this codebase has very minimal comments. I will be adding more comments as and when I get sometime.
* Also this codebase has been made compatible both with Eclipse IDE and IntelliJ IDEA.

## Configuration
Check the [config.properties](src/main/resources/config.properties) and insert your own values and complete the integration of Twitter API to your application by looking at your values from [Twitter Developer Page](https://dev.twitter.com/apps). If you did n't create a Twitter App before, then please create a new Twitter App where you will get all the required values of config.properties afresh and then populate them here without any mistake.<br>
Also, please check [pom.xml](pom.xml) for more information on the various dependencies of the project.<br>

## Requirements
This project uses Maven to build and run the topology.
You need the following on your machine:
* Sun / Oracle JDK >= 1.7.x
* Apache Maven >= 3.0.5
* Requires ZooKeeper, JZMQ, ZeroMQ installed and configured in case of executing this project in distributed mode i.e. Storm Cluster.<br>
	- Follow the steps mentioned [here](https://github.com/nathanmarz/storm/wiki/Setting-up-a-Storm-cluster) for more details on setting up a Storm Cluster.<br>

Rest of the required frameworks and libraries as required are downloaded and configured by Maven, the first time the build is invoked.

## Usage
To build and run this topology, you must use Java 1.7.

### Local Mode:
Local mode can also be run on Windows environment without installing any specific software or framework as such.<br>
In local mode, this application can be run from command line by invoking:<br>

    mvn clean compile exec:java -Dexec.classpathScope=compile -Dexec.mainClass=org.p7h.storm.urlshorten.topology.UnshortenTopology

or

    mvn clean compile package && java -jar target/StormTweetsURLShortening-1.0.0-SNAPSHOT-jar-with-dependencies.jar
	
### Distributed [or Cluster / Production] Mode:
Distributed mode requires a complete and proper Storm Cluster setup. Please refer this [wiki](https://github.com/nathanmarz/storm/wiki/Setting-up-a-Storm-cluster) on setting up a Storm Cluster.<br>
In distributed mode, after starting Nimbus and Supervisors on individual machines, this application can be executed on the master [or Nimbus] machine by invoking the following on the command line:

    storm jar target/StormTweetsURLUnShortening-1.0.0-SNAPSHOT-jar-with-dependencies.jar org.p7h.storm.urlshorten.topology.UnshortenTopology UnshortenURLs

## Problems
If you find any issues, please report them either raising an [issue](https://github.com/P7h/StormTweetsURLUnShortening/issues) here on Github or alert me on my Twitter handle [@P7h](http://twitter.com/P7h). Or even better, please send a [pull request](https://github.com/P7h/StormTweetsURLUnShortening/pulls).
Appreciate your help. Thanks!

## License
Copyright 2013 Prashanth Babu.<br>
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
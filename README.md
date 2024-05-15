# DecPOMDP Solver
This repository contains a spring boot application for solving
decentralized partial-observable markov decision problems.
Solving means that the policies of the agents are optimised
with respective of the expected reward starting from
an initial belief state.

## Getting started
First of all, this project is build on JDK v21.0.2.
In our case [openjdk-21](https://openjdk.org/projects/jdk/21/) was used.
Additionally, you have to have [Maven 3.5+](https://maven.apache.org/download.cgi) installed.
We used Maven 3.9.6.

## How to use
You can run `mvn install` to generate a .jar-file from the source code you can execute.
Otherwise you can run `./mvnw spring-boot:run` to start the application directly.

## How it works
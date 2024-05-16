# DecPOMDP Solver
This repository contains a spring boot application for solving
decentralized partial-observable markov decision problems.
Solving means that the policies of the agents are optimised
with respective of the expected reward starting from
an initial belief state.

## Getting started
First of all, this project is built on JDK v21.0.2.
In our case, [openjdk-21](https://openjdk.org/projects/jdk/21/) was used.
Additionally, you have to have [Maven 3.5+](https://maven.apache.org/download.cgi) installed.
We used Maven 3.9.6.

## How to use
You can run `mvn install` to generate a .jar-file from the source code you can execute.
Otherwise, you can run `./mvnw spring-boot:run` to start the application directly.
On Windows you can run `mvnw.cmd spring-boot:run` instead.

## CLI
This project contains a CLI, which will be started by default.
There you have various commands to solve DecPOMDPs.

### Heuristic policy iteration
The heuristic policy iteration algorithm is based on the algorithm introduced in
"Policy Iteration for Decentralized Control of Markov Decision Processes"
by Bernstein _et al._ from 2009.
It solves DecPOMDPs with the help of stochastic finite state controllers,
which are used to represent the policies of the agents.

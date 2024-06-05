# DecPOMDP Solver
This repository contains a spring boot application for solving
decentralized partial-observable markov decision problems.
Solving means that the policies of the agents are optimized
with respective of the expected reward starting from
an initial belief state.

## Getting started
First of all, this project is built on JDK v21.0.2.
In our case, [openjdk-21](https://openjdk.org/projects/jdk/21/) was used.
Additionally, you have to have [Maven 3.5+](https://maven.apache.org/download.cgi) installed.
We used Maven 3.9.6.

## How to use the shell
You can run `mvn install` to generate a .jar-file from the source code you can execute.
Otherwise, you can run `./mvnw spring-boot:run` to start the application directly.
On Windows you can run `mvnw.cmd spring-boot:run` instead.

### CLI
This project contains a CLI, which will be started by default.
There you have various commands to solve DecPOMDPs.

### Scripts
In the `scripts/` folder are some script files 
that can be used within the Spring shell,
by executing the command `script <file>` or `@<file>`.
For each problem, there exists a script file for solving it.

## How to use Docker
We also provide a Docker image to run this application.
This runs a script to solve the GridSmall problem 
with the heuristic policy iteration solver by default.
This behavior can be configured,
either by setting the environment variable `PROBLEM`,
or by setting the environment variable `SCRIPT`
when running the image.

To do so, you need to build the image by running
```
docker build -t dec-pomdp-solver .
```
and executing the image as a container by running
```
docker run -t dec-pomdp-solver 
```

## Testing
There are many JUnit test suites to check the correctness of the implementation
and to maintain the stability and quality of the implementation.
Those will be automatically executed when calling maven scripts,
like `mvn install`.
If you want to execute those manually, you can run
```
mvn test
```

## Problem instance
Currently, we have three different problem instances in our repository
that can be solved by our heuristic policy iteration implementation.
Here is a list of all problem instances (case-sensitive):

1. DecTiger
2. GridSmall
3. BoxPushing

All of them are originated to the
[MADP Toolbox](https://www.fransoliehoek.net/fb/index.php?fuseaction=software.madp)
by Frans Oliehoek and Matthijs Spaan.

## Solver

### Heuristic policy iteration
The heuristic policy iteration algorithm is based on the algorithm introduced in
"Policy Iteration for Decentralized Control of Markov Decision Processes"
by Bernstein _et al._ from 2009.
It solves DecPOMDPs with the help of stochastic finite state controllers,
which are used to represent the policies of the agents.
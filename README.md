# DecPOMDP Solver
This repository contains a spring boot application for solving 
decentralized partially-observable markov decision problems (DecPOMDPs).
Solving means that the policies of the agents are optimized
with respective of the expected reward starting from an initial belief state.

## Getting started
First, this project is built on JDK v17.0.10.
In our case, [openjdk-17](https://openjdk.org/projects/jdk/17/) was used.
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
Currently, we have five different problem instances in our repository
that can be solved by our heuristic policy iteration implementation.
Here is a list of all problem instances (case-sensitive):

1. DecTiger
2. GridSmall
3. BoxPushing
4. MedicalNanoscaleSystem2
5. MedicalNanoscaleSystem2-skewed

The problems 1 to 3 are originated to the
[MADP Toolbox](https://www.fransoliehoek.net/fb/index.php?fuseaction=software.madp)
by Frans Oliehoek and Matthijs Spaan.
The remaining two are created by the authors and based on the description by
[(Braun *et al.*, 2021)](https://arxiv.org/abs/2110.09152).
Both of them are in the extended `.idpomdp` file format,
which introduces a `partitionSizes` section.

## Solver

### Heuristic policy iteration
The heuristic policy iteration algorithm is based on the algorithm introduced in
"Policy Iteration for Decentralized Control of Markov Decision Processes"
by [(Bernstein *et al.*, 2009)](https://arxiv.org/abs/1401.3460).
It solves DecPOMDPs with the help of stochastic finite state controllers,
which are used to represent the policies of the agents.

This algorithm can be executed with the following commands:

| Command                   | Short Command | Description                                        |
|---------------------------|---------------|----------------------------------------------------|
| `heuristic help`          | `h h`         | Prints information about this algorithm.           |
| `heuristic init`          | `h i`         | Initializes the heuristic policy iteration solver. |
| `heuristic load`          | `h l`         | Loads a problem instance to solve.                 |
| `heuristic initialPolicy` | `h p`         | Sets initial policies for belief point generation. |
| `heuristic solve`         | `h s`         | Solves the loaded problem instance.                |

### Isomorphic heuristic policy iteration
This algorithm is based on the heuristic policy iteration algorithm.
As the name suggests, this algorithm is only applicable to isomorphic DecPOMDPs.
It solves a representative ground DecPOMDP for ranking policies
before it evaluates the policies for the whole DecPOMDP.

This algorithm can be executed with the following commands:

| Command                    | Short Command | Description                                        |
|----------------------------|---------------|----------------------------------------------------|
| `isomorphic help`          | `i h`         | Prints information about this algorithm.           |
| `isomorphic init`          | `i i`         | Initializes the heuristic policy iteration solver. |
| `isomorphic load`          | `i l`         | Loads a problem instance to solve.                 |
| `isomorphic initialPolicy` | `i p`         | Sets initial policies for belief point generation. |
| `isomorphic solve`         | `i s`         | Solves the loaded problem instance.                |

### Representative observations heuristic policy iteration
This algorithm is based on the isomorphic heuristic policy iteration algorithm.
As its predecessor, this algorithm is only applicable to isomorphic DecPOMDPs.
In this case, we additionally assume representative observations,
which means each agent of each partition observes the same for the same history.

This algorithm can be executed with the following commands:

| Command                                    | Short Command | Description                                        |
|--------------------------------------------|---------------|----------------------------------------------------|
| `representativeObservations help`          | `r h`         | Prints information about this algorithm.           |
| `representativeObservations init`          | `r i`         | Initializes the heuristic policy iteration solver. |
| `representativeObservations load`          | `r l`         | Loads a problem instance to solve.                 |
| `representativeObservations initialPolicy` | `r p`         | Sets initial policies for belief point generation. |
| `representativeObservations solve`         | `r s`         | Solves the loaded problem instance.                |

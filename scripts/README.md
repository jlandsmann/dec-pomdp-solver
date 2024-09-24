# Scripts
This directory contains various scripts that contain a sequence of commands.
These scripts can be interpreted by the application and provide
a convenient way of solving DecPOMDPs. 

**[solve-BoxPushing](solve-BoxPushing)**
Solves the BoxPushing problem with the heuristic policy iteration algorithm
with default initial policies and 20 belief points to be generated.

**[solve-DecTiger](solve-DecTiger)**
Solves the DecTiger problem with the heuristic policy iteration algorithm
with 10 belief points to be generated.
As initial policies each agent choses to listen with a probability of 80%,
and to open either left or right door with a probability of 10% each.

**[solve-GridSmall](solve-GridSmall)**
Solves the GridSmall problem with the heuristic policy iteration algorithm
with default initial policies and 10 belief points to be generated.

**[solve-ground-MedicalNanoscale2](solve-ground-MedicalNanoscale2)**
Transforms the MedicalNanoscale2 problem into a ground DecPOMDP 
and solves it with the heuristic policy iteration algorithm
without initial policies and 10 belief points.

**[solve-iso-MedicalNanoscale2](solve-iso-MedicalNanoscale2)**
Solves the MedicalNanoscale2 problem with the isomorphic heuristic policy iteration algorithm
with default initial policies and 10 belief points to be generated.

**[solve-MedicalNanoscale2](solve-MedicalNanoscale2)**
Solves the MedicalNanoscale2 problem with the representative observations heuristic policy iteration algorithm
with default initial policies and 10 belief points to be generated.
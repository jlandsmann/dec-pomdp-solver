# Problems
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
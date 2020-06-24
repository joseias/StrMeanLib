## Java implementation of several heuristics to compute the median string:

To run different algorithms, modify the file conf.properties to set the heuristic, scoring matrix, etc.

#### Algorithms:

[1] Sánchez P, Abreu J, Seco D. “Assessing the best edit in perturbation-based iterative refinement algorithms to compute the median string” Pattern Recognition Letters. Vol. 120, pp. 104-111, 2019.

Set OPS_STAT_EVALUATOR=strmean.opstateval.OpStatsP to use this algorithm.

[2] Abreu J. and Rico-Juan J, “A New Iterative Algorithm for Computing a Quality Approximate Median of Strings based on Edit Operations.” Pattern Recognition Letters. Vol. 36, pp. 74-80, 2014.

Set OPS_STAT_EVALUATOR=strmean.opstateval.OpStatsJR to use this algorithm.



#### Dependencies:
	-

### Run example:
    java -cp StrMean.jar strmean.malgorithms.MAJRStatistical test.in test.out


#### Command line options:
	-

### Related works
[1] Sánchez P, Abreu J, Seco D. “Assessing the best edit in perturbation-based iterative refinement algorithms to compute the median string” Pattern Recognition Letters. Vol. 120, pp. 104-111, 2019.

[2] Abreu J. and Rico-Juan J, “A New Iterative Algorithm for Computing a Quality Approximate Median of Strings based on Edit Operations.” Pattern Recognition Letters. Vol. 36, pp. 74-80, 2014.

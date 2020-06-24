## Java implementation of several heuristics to compute the median string:

Modify conf.properties to select the algorithm, scoring matrix, etc.

#### Algorithms:

[1] ABREU, J. y J.R. RICO-JUAN, . "A new iterative algorithm for computing a quality approximate median of strings based on edit operations". 
Pattern Recognition Letters. 2014, vol 36, núm. 0, p. 74 - 80.

Set OPS_STAT_EVALUATOR=strmean.opstateval.OpStatsJR to use this algorithm.

[2] MIRABAL, P., J. ABREU,  y D. SECO, . "Assessing the best edit in perturbation-based iterative refinement algorithms to compute the median string". 
Pattern Recognition Letters. 2019, vol 120, p. 104 - 111.

Set OPS_STAT_EVALUATOR=strmean.opstateval.OpStatsP to use this algorithm.

#### Dependencies:
	-

### Run example:
    java -cp StrMean.jar strmean.malgorithms.MAJRStatistical test.in test.out


#### Command line options:
	-

### Related works
[1] Sánchez P, Abreu J, Seco D. “Assessing the best edit in perturbation-based iterative refinement algorithms to compute the median string” Pattern Recognition Letters. Vol. 120, pp. 104-111, 2019.

[2] Abreu J. and Rico-Juan J, “A New Iterative Algorithm for Computing a Quality Approximate Median of Strings based on Edit Operations.” Pattern Recognition Letters. Vol. 36, pp. 74-80, 2014.

## StrMeanLib
StrMeanLib is an open-source Java implementation of several heuristics to compute the median string:

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

### Citation 
StrMeanLib is provided as free open-source software. If you have used ProMetrics in your StrMeanLib, authors will be grateful if you fill and submit to [joseias@gmail.com](joseias@gmail.com) the following [Usage-Acknowledgment-StrMeanLib](https://github.com/joseias/StrMeanLib/blob/master/Usage-Acknowledgment-StrMeanLib.docx) acknowledging our contribution. If citing, please reference to:

Abreu J. and Rico-Juan J, “[A New Iterative Algorithm for Computing a Quality Approximate Median of Strings based on Edit Operations.](https://www.sciencedirect.com/science/article/pii/S0167865513003504)” Pattern Recognition Letters. Vol. 36, pp. 74-80, 2014.


### Related works
[1] Sánchez P, Abreu J, Seco D. “Assessing the best edit in perturbation-based iterative refinement algorithms to compute the median string” Pattern Recognition Letters. Vol. 120, pp. 104-111, 2019.


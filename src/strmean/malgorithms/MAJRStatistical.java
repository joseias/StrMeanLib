package strmean.malgorithms;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.opstateval.OpStats;
import strmean.data.Operation;
import strmean.data.SymbolDif;
import strmean.distances.EditDistance;
import strmean.main.JConstants;
import strmean.main.JMathUtils;
import strmean.main.JUtils;


/**
 * Implements a generic one-edit-at-a-time perturbation-based algorithm for the median string problem.
 * Different algorithms can be instantiated by implementing different quality metrics
 * for the operations.
 * Described in:
 * ABREU, J. y J.R. RICO-JUAN, .
 * "A new iterative algorithm for computing a quality approximate median of strings based on edit operations".
 * Pattern Recognition Letters. 2014, vol 36, pp. 74 - 80.
 */
public class MAJRStatistical extends MAlgorithm {

    OpStats opStatsTemplate;

    @Override
    public MAResult getMean(List<Example> BD, Example seed, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        PrintStream oplog = (PrintStream) p.get(JConstants.LOG_FILE);
        /*logger for printing best operation each time*/

        int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));
        opStatsTemplate = JUtils.newInstance(OpStats.class, p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        long maxEpoch = Long.parseLong(p.getProperty(JConstants.MAX_EPOCH, "0"));
        int maxOps = Integer.parseInt(p.getProperty(JConstants.MAX_OPS, "0"));

        String cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> comparator = JUtils.newInstance(Comparator.class, cmpType);

        boolean pruneNegQuality = Boolean.parseBoolean(p.getProperty(JConstants.PRUNE_NEG_QUALITY));
        //</editor-fold>

        String logs;

        boolean changed;
        boolean keepTryingOps;

        Example actualCandidate = new Example(seed);
        Example bestExample = new Example(actualCandidate);

        List<Operation> ops;
        List<Operation> opsTmp;
        List<String> opPosL = new LinkedList<>();

        HashMap<String, Example> procExamples = new HashMap<>();

        OpStats opStatsBestExample;
        Operation op;

        long cEpoch = 0;
        int opPos;
        int cOps;
        int totalDist = 0;

        //<editor-fold defaultstate="collapsed" desc="computing distances and stats">
        OpStats opStatsCandidate = this.testExample(bestExample, BD, Float.MAX_VALUE, p);
        totalDist = totalDist + opStatsCandidate.totalDist;

        opStatsBestExample = opStatsCandidate;

        procExamples.put(new String(bestExample.sequence), bestExample);
        //</editor-fold>
        do {
            cEpoch++;
            changed = false;

            //<editor-fold defaultstate="collapsed" desc="get and sort edit operations">
            ops = opStatsCandidate.getOperations();

            //<editor-fold defaultstate="collapsed" desc="pruning, discard opp with quality <= 0 ">
            if (pruneNegQuality) {
                ops.removeIf(e -> {
                    return e.opInfo.quality <= 0;
                });
            }
            //</editor-fold>

            Collections.sort(ops, comparator);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="evaluate operations by ranking">

            Iterator<Operation> itOps = ops.iterator();
            cOps = 0;
            opPos = 1;
            keepTryingOps = true;

            while (itOps.hasNext() && keepTryingOps == true) {
                op = itOps.next();

                if (op.type != 's' || op.a != op.b) /*if not a substituion by the same symbol*/ {
                    //<editor-fold defaultstate="collapsed" desc="get the new incumbent">
                    opsTmp = new ArrayList<>(1);
                    opsTmp.add(op);
                    actualCandidate = bestExample.applyOperations(opsTmp);

                    //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="assess the incumbent">
                    String key = new String(actualCandidate.sequence);
                    if (!procExamples.containsKey(key)) {
                        /* if can be found in the table, shall not be the better than bestExample*/
                        opStatsCandidate = this.testExample(actualCandidate, BD, opStatsBestExample.sumDist, p);
                        totalDist = totalDist + opStatsCandidate.totalDist;

                        procExamples.put(key, actualCandidate);

                        if (opStatsBestExample.sumDist > opStatsCandidate.sumDist) {
                            double meanOld = JMathUtils.round(opStatsBestExample.sumDist / BD.size(), precision);
                            double meanNew = JMathUtils.round(opStatsCandidate.sumDist / BD.size(), precision);
                            double stdvNew = JMathUtils.round(JMathUtils.getStdv(opStatsCandidate.distances, meanNew), precision);

                            double expectedV = JMathUtils.round(op.opInfo.quality / BD.size(), precision);
                            double deltha = JMathUtils.round((meanOld - meanNew), precision);

                            bestExample = actualCandidate;
                            opStatsBestExample = opStatsCandidate;
                            changed = true;

                            String info = meanOld + " " + meanNew + " " + deltha + " " + expectedV + " " + stdvNew + " " + totalDist;
                            logs = Integer.toString(opPos) + "-" + op.toString() + " " + info;
                            opPosL.add(logs);
                            oplog.println(logs);
                        } else {
                            if (maxEpoch == 1) {
                                /*patch to print the  operation if maxEpoch==1*/
                                logs = Integer.toString(opPos) + "-" + op.toString();
                                opPosL.add(logs);
                                oplog.println(logs);
                            }
                        }
                    }
                    //</editor-fold>
                    opPos++;
                    cOps++;
                }

                keepTryingOps = !changed && ((maxOps <= 0) || (maxOps > 0 && cOps < maxOps));
            }
            //</editor-fold>
            /* continue if there was an improvement, or max Epoch are reached, if specified*/
        } while (changed == true && ((maxEpoch <= 0) || (maxEpoch > 0 && cEpoch < maxEpoch)));

        MAResult result = new MAResult();
        result.meanExample = bestExample;
        result.sumDist = opStatsBestExample.sumDist;
        result.totalDist = totalDist;
        result.opPosList = opPosL;
        result.distances = opStatsBestExample.distances;
        return result;

    }

    /**
     * *
     * Requires a properties object with: - EditDistance object, mapped with key
     * JConstants.EDIT_DISTANCE
     *
     * Warning: This method relies in the previous initialization of
     * opStatsTemplate this is to avoid repeated creations of OpStats objects by
     * reflection...
     *
     * @param candidate
     * @param BD
     * @param thresholdDist
     * @param p
     * @return
     */
    protected OpStats testExample(Example candidate, List<Example> BD, float thresholdDist, Properties p) {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        OpStats opStats = opStatsTemplate.newInstance();
        opStats.init(candidate, ed._sd, BD.size());

        EDResult edR;
        int index = 0;
        for (Example e : BD) {
            edR = ed.dEdition(candidate, e, true);
            opStats.totalDist++;
            opStats.sumDist = opStats.sumDist + edR.dist;
            opStats.distances[index] = edR.dist;
            index++;

            /*optimization to speed up the test*/
            if (opStats.sumDist > thresholdDist) {
                /*abort execution*/
                return opStats;
            }

            for (Operation op : edR.getOperations()) {
                op.opInfo.weigth = e.weigth;
                opStats.addOperation(op);
            }
        }

        return opStats;
    }

    public static void main(String[] args) {
        JUtils.initLogger();

        try {

            String inpath = args[0];
            String outpath = args[1];

            //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
            Properties p = JUtils.loadProperties();
            String sdType = p.getProperty(JConstants.SYMBOL_DIF);
            SymbolDif sd = JUtils.newInstance(SymbolDif.class, sdType, p);
            p.put(JConstants.SYMBOL_DIF, sd);

            String edType = p.getProperty(JConstants.EDIT_DISTANCE);
            EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
            p.put(JConstants.EDIT_DISTANCE, eD);
            //</editor-fold>

            List<Example> BD = JUtils.loadExamples(inpath);
            PrintStream oplog;
            PrintStream ps = new PrintStream(outpath);

            oplog = new PrintStream(outpath + ".log");
            p.put(JConstants.LOG_FILE, oplog);
            int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));

            MAJRStatistical js = new MAJRStatistical();
            MASet sm = new MASet();
            MAResult setMean = sm.getMean(BD, null, p);

            ps.println("SetMedian AvgDist: " + setMean.sumDist / BD.size() + " TotalDist: " + setMean.totalDist);
            ps.println(setMean.meanExample.toString());

            long start = System.currentTimeMillis();
            MAResult newMean = js.getMean(BD, setMean.meanExample, p);
            long end = System.currentTimeMillis();

            int totalDist = newMean.totalDist + setMean.totalDist;
            double median = newMean.sumDist / BD.size();
            double stdv = JMathUtils.round(JMathUtils.getStdv(newMean.distances, median), precision);

            ps.println("Mean AvgDist: " + newMean.sumDist / BD.size() + " TotalDist: " + totalDist + " Stdv: " + stdv);
            ps.println(newMean.meanExample.toString());
            ps.println("PFO " + (end - start));

            newMean.opPosList.forEach((s) -> {
                ps.println(s);
            });
            oplog.close();
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }
}

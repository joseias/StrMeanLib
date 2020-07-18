package strmean.malgorithms;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.data.Operation;
import strmean.data.SymbolDif;
import strmean.distances.EditDistance;
import strmean.main.JConstants;
import strmean.main.JMathUtils;
import strmean.main.JUtils;
import strmean.opstateval.OpStats;

/**
 * Implement the multiple-edits-at-a-time algorithm for the median string
 * problem described in: FISCHER, Igor y ANDREAS ZELL. String Averages and
 * Self-Organizing Map for Strings. Proc. of the Neural Computation. 2000, pp.
 * 208-215.
 */
public class MAFischer2000 extends MAlgorithm {

    OpStats opStatsTemplate;

    @Override
    public MAResult getMean(List<Example> BD, Example seed, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        opStatsTemplate = JUtils.newInstance(OpStats.class, p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        //</editor-fold>
        boolean improved;
        Example actualCandidate = new Example(seed);
        Example bestExample = new Example(actualCandidate);
        List<Operation> ops;
        HashMap<String, Example> procExamples = new HashMap<>();

        OpStats opStatsCandidate;
        OpStats opStatsBestExample;

        int totalDist = 0;
        //<editor-fold defaultstate="collapsed" desc="computing distances and stats">
        opStatsCandidate = this.testExample(bestExample, BD, Float.MAX_VALUE, p);
        totalDist = totalDist + opStatsCandidate.totalDist;
        opStatsBestExample = opStatsCandidate;

        procExamples.put(new String(bestExample.sequence), bestExample);
        //</editor-fold>
        do {
            improved = false;

            //<editor-fold defaultstate="collapsed" desc="select the operations to be applied">
            ops = this.selectOperations(opStatsCandidate, BD.size(), p);

            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="get the new incumbent">
            actualCandidate = bestExample.applyOperations(ops);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="assess the incumbent">
            String key = new String(actualCandidate.sequence);
            if (!procExamples.containsKey(key)) {
                /* if can be found in the table, shall not be the better than bestExample*/
                opStatsCandidate = this.testExample(actualCandidate, BD,opStatsBestExample.sumDist, p);
                totalDist = totalDist + opStatsCandidate.totalDist;

                procExamples.put(key, actualCandidate);

                if (opStatsBestExample.sumDist > opStatsCandidate.sumDist) {
                    bestExample = actualCandidate;
                    opStatsBestExample = opStatsCandidate;

                    improved = true;
                }
            }
            //</editor-fold>
        } while (improved == true);

        MAResult result = new MAResult();
        result.meanExample = bestExample;
        result.sumDist = opStatsBestExample.sumDist;
        result.totalDist = totalDist;
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

    /**
     * Given all the possible edit operations, return the best for each
     * position.
     * 
     * @param opStats
     * @param DBSize
     * @param p
     * @return
     * @throws java.lang.Exception
     */
    protected List<Operation> selectOperations(OpStats opStats, int DBSize, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependenciess">
        String cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> comparator = JUtils.newInstance(Comparator.class, cmpType);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="initializations ">
        List<Operation> ops = opStats.getOperations();

        ArrayList[] posOps = new ArrayList[opStats.ex.sequence.length + 1];
        ArrayList[] posOpsI = new ArrayList[opStats.ex.sequence.length + 1];

        List<Operation> selectedOps = new ArrayList<>(opStats.ex.sequence.length);

        for (int i = 0; i < posOps.length; i++) {
            posOps[i] = new ArrayList<>();
            posOpsI[i] = new ArrayList<>();
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="set each operation in the respective list (per position)">
        int pos;

        for (Operation op : ops) {
            pos = op.posSource;
            if (op.type == 'i') {
                posOpsI[pos].add(op);
            } else {
                posOps[pos].add(op);
            }

        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="sort operations within the same position by quality and select the best">
        Operation tmp;
        for (int i = 0; i < posOps.length; i++) {
            //<editor-fold defaultstate="collapsed" desc="insertions">
            if (!posOpsI[i].isEmpty()) {
                Collections.sort(posOpsI[i], comparator);
                tmp = (Operation) posOpsI[i].get(0);
                if (tmp.opInfo.quality >= DBSize / 2) {
                    selectedOps.add(tmp);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="substitutions and deletions">
            if (!posOps[i].isEmpty()) /* this would be the case every time*/ {
                Collections.sort(posOps[i], comparator);
                tmp = (Operation) posOps[i].get(0);
                selectedOps.add(tmp);
            }
            //</editor-fold> 
        }
        //</editor-fold>
        return selectedOps;
    }

    private void printOperations(ArrayList<Operation> ops) {
        for (Operation op : ops) {
            char a = ' ';
            char b = ' ';

            switch (op.type) {
                case 's':
                    a = op.a;
                    b = op.b;
                    break;
                case 'i':
                    a = 'e';
                    b = op.b;
                    break;
                case 'd':
                    a = op.a;
                    b = 'e';
                    break;

            }
            String v = Float.toString(op.opInfo.quality);
            switch (v.length()) {
                case 1:
                    v = "00".concat(v);
                    break;
                case 2:
                    v = "0".concat(v);
                    break;
            }
            System.out.print(op.type + ":" + a + ":" + b + ":" + op.posSource + ":" + v + " ");
        }
        System.out.println();

    }

    public static void main(String[] args) throws Exception {
      JUtils.initLogger();

        try {

            Path inpath = Paths.get(args[0]);
            Path outpath = Paths.get(args[1]);
            String outname = outpath.getFileName().toString();
            String outdir = outpath.getParent().toString();
            String sep = System.getProperty("file.separator");

            //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
            Properties p = JUtils.loadProperties();
            String sdType = p.getProperty(JConstants.SYMBOL_DIF);
            SymbolDif sd = JUtils.newInstance(SymbolDif.class, sdType, p);
            p.put(JConstants.SYMBOL_DIF, sd);

            String edType = p.getProperty(JConstants.EDIT_DISTANCE);
            EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
            p.put(JConstants.EDIT_DISTANCE, eD);
            //</editor-fold>

            List<Example> BD = JUtils.loadExamples(inpath.toString());      
            
            PrintStream psout = new PrintStream(outpath.toString());
            PrintStream pslog;
            pslog = new PrintStream(outdir+sep+outname+".log");
            p.put(JConstants.LOG_FILE, pslog);
            int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));

            MAFischer2000 js = new MAFischer2000();
            MASet sm = new MASet();
            
            MAResult setMean = sm.getMean(BD, null, p);
            double median = setMean.sumDist / BD.size();
            int totalDist = setMean.totalDist;
            double stdv = JMathUtils.round(JMathUtils.getStdv(setMean.distances, median), precision);
            psout.println("SetMedian AvgDist: " + median + " TotalDist: " + totalDist + " Stdv: " + stdv);
            psout.println(setMean.meanExample.toString());


            MAResult newMeanE = js.getMean(BD, new Example("-", ""), p);
            median = newMeanE.sumDist / BD.size();
            totalDist = newMeanE.totalDist;
            stdv = JMathUtils.round(JMathUtils.getStdv(newMeanE.distances, median), precision);
            psout.println("Empty AvgDist: " + median + " TotalDist: " + totalDist + " Stdv: " + stdv);
            psout.println(newMeanE.meanExample.toString());
            
            
            MAResult newMeanM = js.getMean(BD, setMean.meanExample, p);
            median = newMeanM.sumDist / BD.size();
            totalDist = newMeanM.totalDist + setMean.totalDist;
            stdv = JMathUtils.round(JMathUtils.getStdv(newMeanM.distances, median), precision);
            psout.println("Mean AvgDist: " + median + " TotalDist: " + totalDist + " AddDist: " + newMeanM.totalDist + " Stdv: " + stdv);
            psout.println(newMeanM.meanExample.toString());
            
            pslog.close();
            psout.close();
        } catch (Exception e) {
            System.err.print(e.getMessage());
        }
    }
}

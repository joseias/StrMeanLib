/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author jabreu
 */
public class MAJRStatistical extends MAlgorithm {

    OpStats opStatsTemplate;

    @Override
    public MAResult getMean(List<Example> a_BD, Example a_seed, Properties p) throws Exception {
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        PrintStream log = (PrintStream) p.get(JConstants.LOG_FILE);
        int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));
        opStatsTemplate = (OpStats) JUtils.buildInstanceFromType(p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        long maxEpoch = Long.parseLong(p.getProperty(JConstants.MAX_EPOCH, "0"));
        int maxOps = Integer.parseInt(p.getProperty(JConstants.MAX_OPS, "0"));

        String m_cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> m_comparator = (Comparator<Operation>) JUtils.buildInstanceFromType(m_cmpType);

        boolean pruneNegQuality = Boolean.parseBoolean(p.getProperty(JConstants.PRUNE_NEG_QUALITY));
        //</editor-fold>

        String logs;

        boolean m_changed;
        boolean keepTryingOps;

        Example m_actualCandidate = new Example(a_seed);
        Example m_bestExample = new Example(m_actualCandidate);

        List<Operation> m_ops;
        List<Operation> m_opsTmp;
        List<String> opPosL = new LinkedList<>();

        HashMap<String, Example> m_procExamples = new HashMap<>();

        OpStats opStatsBestExample;
        Operation op;

        long cEpoch = 0;
        int opPos;
        int cOps;
        int totalDist = 0;

        //<editor-fold defaultstate="collapsed" desc="Calcular distancias y estadisticas">
        OpStats opStatsCandidate = this.testExample(m_bestExample, a_BD, Float.MAX_VALUE, p);
        totalDist = totalDist + opStatsCandidate.totalDist;

        opStatsBestExample = opStatsCandidate;

        m_procExamples.put(new String(m_bestExample.sequence), m_bestExample);
        //</editor-fold>
        do {
//            System.err.println(epoch);
            cEpoch++;
            m_changed = false;

            //<editor-fold defaultstate="collapsed" desc="Obtener operaciones y ordenar de acuerdo al criterio dado">
            m_ops = opStatsCandidate.getOperations();

            //<editor-fold defaultstate="collapsed" desc="Poda: Eliminar opp con quality <= 0 (171114)">
            if (pruneNegQuality) {
                m_ops.removeIf(e -> {
                    return e.opInfo.quality <= 0;
                });
            }
            //</editor-fold>

            Collections.sort(m_ops, m_comparator);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Probar las operaciones de acuerdo a su ranking">

            Iterator<Operation> itOps = m_ops.iterator();
            cOps = 0;
            opPos = 1;
            keepTryingOps = true;

            while (itOps.hasNext() && keepTryingOps == true) {
                op = itOps.next();

                if (op.type != 's' || op.a != op.b) /*Si no es una sustitucion de un simbolo por si mismo*/ {
                    //<editor-fold defaultstate="collapsed" desc="Obtener nueva candidata">
                    m_opsTmp = new ArrayList<>(1);
                    m_opsTmp.add(op);
                    m_actualCandidate = m_bestExample.applyOperations(m_opsTmp);

                    //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Probar candidata">
                    String key = new String(m_actualCandidate.sequence);
                    if (!m_procExamples.containsKey(key)) {
                        /*Si est\'{a} en la tabla no deber\'{i}a ser mejor que bestExample*/
                        opStatsCandidate = this.testExample(m_actualCandidate, a_BD, opStatsBestExample.sumDist, p);
                        totalDist = totalDist + opStatsCandidate.totalDist;

                        m_procExamples.put(key, m_actualCandidate);

                        if (opStatsBestExample.sumDist > opStatsCandidate.sumDist) {
                            double meanOld = JMathUtils.round(opStatsBestExample.sumDist / a_BD.size(), precision);
                            double meanNew = JMathUtils.round(opStatsCandidate.sumDist / a_BD.size(), precision);
                            double stdvNew = JMathUtils.round(JMathUtils.getStdv(opStatsCandidate.distances, meanNew), precision);

                            double expectedV = JMathUtils.round(op.opInfo.quality / a_BD.size(), precision);
                            double deltha = JMathUtils.round((meanOld - meanNew), precision);

                            m_bestExample = m_actualCandidate;
                            opStatsBestExample = opStatsCandidate;
                            m_changed = true;

                            String info = meanOld + " " + meanNew + " " + deltha + " " + expectedV + " " + stdvNew + " " + totalDist;
                            logs = Integer.toString(opPos) + "-" + op.toString() + " " + info;
                            opPosL.add(logs);
                            log.println(logs);
                        }
                        /*JComment: Patch to print the  operation if maxEpoch==1*/
                        else {
                            if (maxEpoch == 1) {
                                logs = Integer.toString(opPos) + "-" + op.toString();
                                opPosL.add(logs);
                                log.println(logs);
                            }
                        }
                    }
                    //</editor-fold>
                    opPos++;
                    cOps++;
                }

                keepTryingOps = !m_changed && ((maxOps <= 0) || (maxOps > 0 && cOps < maxOps));
            }
            //</editor-fold>
        } /*Continue if there was an improvement, or max Epoch are reached, if specified*/ while (m_changed == true && ((maxEpoch <= 0) || (maxEpoch > 0 && cEpoch < maxEpoch)));

        MAResult result = new MAResult();
        result.meanExample = m_bestExample;
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
     * @param a_candidate
     * @param a_BD
     * @param thresholdDist
     * @param p
     * @return
     */
    protected OpStats testExample(Example a_candidate, List<Example> a_BD, float thresholdDist, Properties p) {

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        OpStats opStats = opStatsTemplate.newInstance();
        opStats.init(a_candidate, ed._sd, a_BD.size());

        EDResult edR;
        int index = 0;
        for (Example e : a_BD) {
            edR = ed.dEdition(a_candidate, e, true);
            opStats.totalDist++;
            opStats.sumDist = opStats.sumDist + edR.dist;
            opStats.distances[index] = edR.dist;
            index++;

            /*Optimization to speed up the test*/
            if (opStats.sumDist > thresholdDist) {
                /*Abort execution*/
                return opStats;
            }

            for (Operation op : edR.getOperations()) {
                op.opInfo.weigth = e.weigth;
                opStats.addOperation(op);
            }
        }

        return opStats;
    }

    public static void main(String[] args) throws Exception {
        JUtils.initLogger();
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p = JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif) JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);

        String m_edType = p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD = (EditDistance) JUtils.buildInstanceFromType(m_edType, p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>

//        System.out.println("Working...");
        List<Example> m_BD = JUtils.loadExamples(args[0]);
        PrintStream ps = new PrintStream(args[1]);
        PrintStream pslog = new PrintStream(args[1] + ".log");
        p.put(JConstants.LOG_FILE, pslog);

        int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));

        MAJRStatistical m_js = new MAJRStatistical();
        MASet m_sm = new MASet();

        MAResult m_setMean = m_sm.getMean(m_BD, null, p);
        ps.println("SetMedian AvgDist: " + m_setMean.sumDist / m_BD.size() + " TotalDist: " + m_setMean.totalDist);
        ps.println(m_setMean.meanExample.toString());

        long start = System.currentTimeMillis();
        MAResult m_newMean = m_js.getMean(m_BD, m_setMean.meanExample, p);
        long end = System.currentTimeMillis();

        int totalDist = m_newMean.totalDist + m_setMean.totalDist;
        double median = m_newMean.sumDist / m_BD.size();
        double stdv = JMathUtils.round(JMathUtils.getStdv(m_newMean.distances, median), precision);
        ps.println("Mean AvgDist: " + m_newMean.sumDist / m_BD.size() + " TotalDist: " + totalDist + " Stdv: " + stdv);
        ps.println(m_newMean.meanExample.toString());

        ps.println("PFO " + (end - start));
//        for(String s:m_newMean.opPosList){
//            ps.println(s);
//        }

        ps.close();
        pslog.close();
    }
}

package strmean.malgorithms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import strmean.data.ComparatorOperationPosInv;
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
 * Implements multiple operations at a time Date: 2020/07/01
 */
public class MAJRStatisticalMultiOp extends MAlgorithm {

    OpStats opStatsTemplate;

    @Override
    public MAResult getMean(List<Example> iset, Example a_seed, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        PrintStream oplog = (PrintStream) p.get(JConstants.LOG_FILE);
        /*logger for printing best operation each time*/

        int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));
        opStatsTemplate = JUtils.newInstance(OpStats.class, p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        long maxEpoch = Long.parseLong(p.getProperty(JConstants.MAX_EPOCH, "0"));
        int maxOps = Integer.parseInt(p.getProperty(JConstants.MAX_OPS, "0"));

        String cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> comparator = JUtils.newInstance(Comparator.class, cmpType);
        p.put(JConstants.COMPARATOR_OPS, comparator);
        boolean pruneNegQuality = Boolean.parseBoolean(p.getProperty(JConstants.PRUNE_NEG_QUALITY));

        //for simulated annealing
        double T = 1; //temperature
        // simulated annealing parameters 
        double Tmin = .0001;
        double alpha = 0.9;
        //</editor-fold>

        //for log contest
        File file = new File(p.getProperty(JConstants.MULTI_DATA) + ".log");
        FileOutputStream fos = new FileOutputStream(file, true);
        PrintStream psLog = new PrintStream(fos);
        //end for log contest
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

        //<editor-fold defaultstate="collapsed" desc="computing distances and stats">
        OpStats opStatsCandidate = this.testExample(m_bestExample, iset, Float.MAX_VALUE, p);
        totalDist = totalDist + opStatsCandidate.totalDist;

        opStatsBestExample = opStatsCandidate;

        m_procExamples.put(new String(m_bestExample.sequence), m_bestExample);
        //</editor-fold>
        do {
            cEpoch++;
            m_changed = false;

            //<editor-fold defaultstate="collapsed" desc="get and sort edit operations">
            m_ops = opStatsCandidate.getOperations();
            // Here we search for the best operation set to apply
            ArrayList<ArrayList<Operation>> mpp = bestByPos(opStatsCandidate, p);
            List<Operation> mejores = new ArrayList<>();

            int epsilon = Integer.parseInt(p.getProperty("EPSILON", "3"));
            int maxCant = Integer.parseInt(p.getProperty("MAX_MULTY_OP", "2"));

            bestEditions(mejores, mpp, 0, mpp.size(), epsilon, maxCant);
            ComparatorOperationPosInv coi = new ComparatorOperationPosInv();
            Collections.sort(mejores, coi);
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="evaluate operations by ranking">

            cOps = 0;
            opPos = 1;
            keepTryingOps = true;

            while (mejores.size() > 0 && keepTryingOps == true) {
                //<editor-fold defaultstate="collapsed" desc="get the new incumbent">
                m_actualCandidate = m_bestExample.applyOperations(mejores);
                op = mejores.get(0); //esto simplemente es para no afectar el código que se imprime, realmente no refleja ahora todas las operaciones que se aplican
                //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="assess the incumbent">
                String key = new String(m_actualCandidate.sequence);
                if (!m_procExamples.containsKey(key)) {
                    /* if can be found in the table, shall not be the better than bestExample*/
                    opStatsCandidate = this.testExample(m_actualCandidate, iset, opStatsBestExample.sumDist, p);
                    totalDist = totalDist + opStatsCandidate.totalDist;

                    m_procExamples.put(key, m_actualCandidate);

                    double ap = 0;
                    if (T > Tmin) {

                        ap = Math.pow(Math.E, (opStatsBestExample.sumDist - opStatsCandidate.sumDist) / T * iset.size());
                    }
                    double rand = Math.random();
                    if (opStatsBestExample.sumDist > opStatsCandidate.sumDist || ap > rand) {
                        double meanOld = JMathUtils.round(opStatsBestExample.sumDist / iset.size(), precision);
                        double meanNew = JMathUtils.round(opStatsCandidate.sumDist / iset.size(), precision);
                        double stdvNew = JMathUtils.round(JMathUtils.getStdv(opStatsCandidate.distances, meanNew), precision);

                        double expectedV = JMathUtils.round(op.opInfo.quality / iset.size(), precision);
                        double deltha = JMathUtils.round((meanOld - meanNew), precision);

                        m_bestExample = m_actualCandidate;
                        opStatsBestExample = opStatsCandidate;
                        m_changed = true;

                        String info = meanOld + " " + meanNew + " " + deltha + " " + expectedV + " " + stdvNew + " " + totalDist;
                        opPosL.add(Integer.toString(opPos) + "-" + op.toString() + " " + info);
                        // log contest
                        psLog.println(Long.toString(System.currentTimeMillis()) + ":");
                        psLog.println(m_bestExample.sequence);
                        psLog.println(opStatsBestExample.sumDist / iset.size());
                        //end  log contest

                        T *= alpha;

                    } /*patch to print the  operation if maxEpoch==1*/ else {
                        if (maxEpoch == 1) {
                            opPosL.add(Integer.toString(opPos) + "-" + op.toString());
                        }
                    }
                }
                //</editor-fold>
                opPos++;
                cOps++;

                mejores.clear();
                bestEditions(mejores, mpp, 0, mpp.size(), epsilon, maxCant);
                keepTryingOps = !m_changed && ((maxOps <= 0) || (maxOps > 0 && cOps < maxOps));
            }
            //</editor-fold>
            /* continue if there was an improvement, or max Epoch are reached, if specified*/
        } while (m_changed == true && ((maxEpoch <= 0) || (maxEpoch > 0 && cEpoch < maxEpoch)));

        MAResult result = new MAResult();
        result.meanExample = m_bestExample;
        result.sumDist = opStatsBestExample.sumDist;
        result.totalDist = totalDist;
        result.opPosList = opPosL;
        result.distances = opStatsBestExample.distances;
        psLog.close();
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
     * @param iset
     * @param thresholdDist
     * @param p
     * @return
     */
    protected OpStats testExample(Example candidate, List<Example> iset, float thresholdDist, Properties p) {
        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        OpStats opStats = opStatsTemplate.newInstance();
        opStats.init(candidate, ed._sd, iset.size());

        EDResult edR;
        int index = 0;
        for (Example e : iset) {
            edR = ed.dEdition(candidate, e, true);
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
                opStats.addOperation(op);
            }
        }

        return opStats;
    }

    public ArrayList<ArrayList<Operation>> bestByPos(OpStats mediaCandidata, Properties p) {
        List<Operation> candidatas = mediaCandidata.getOperations();
        ArrayList<ArrayList<Operation>> mejores = new ArrayList<>();
        for (int i = 0; i < mediaCandidata.ex.sequence.length; i++) {
            mejores.add(new ArrayList<>());
        }
        for (Operation candidata : candidatas) {
            if ((candidata.type != 's' || candidata.a != candidata.b) && candidata.opInfo.quality > -100) {
                mejores.get(candidata.posSource).add(candidata);
            }
        }
        Comparator<Operation> comparator = (Comparator<Operation>) p.get(JConstants.COMPARATOR_OPS);

        for (int i = 0; i < mejores.size(); i++) {
            Collections.sort(mejores.get(i), comparator);
        }

        return mejores;
    }

    public int findMaxOp(ArrayList<ArrayList<Operation>> mejoresPorPosicion, int desde, int hasta) {
        if (desde >= 0 && hasta <= mejoresPorPosicion.size() && desde < hasta) {
            int maxPos = -1;
            float maxValue = Float.MIN_VALUE;
            for (int i = desde; i < hasta; i++) {
                if (mejoresPorPosicion.get(i).size() > 0 && mejoresPorPosicion.get(i).get(0).opInfo.quality > maxValue) {
                    maxValue = mejoresPorPosicion.get(i).get(0).opInfo.quality;
                    maxPos = i;
                }
            }
            return maxPos;
        } else {
            return -1;
        }

    }

    public void bestEditions(List<Operation> salida, ArrayList<ArrayList<Operation>> mejoresPorPosicion, int desde, int hasta, int epsilon, int salidaMaxSize) {
        if (salida.size() < salidaMaxSize) {
            int mejorPos = findMaxOp(mejoresPorPosicion, desde, hasta);
            if (mejorPos != -1) {
                salida.add(mejoresPorPosicion.get(mejorPos).get(0));
                mejoresPorPosicion.get(mejorPos).remove(0);
                bestEditions(salida, mejoresPorPosicion, desde, mejorPos - epsilon, epsilon, salidaMaxSize);
                bestEditions(salida, mejoresPorPosicion, mejorPos + epsilon, hasta, epsilon, salidaMaxSize);
            }
        }

    }

    public static void main(String[] args) {
        JUtils.initLogger();
        try {
            Path inpath = Paths.get(args[0]);
            Path outpath = Paths.get(args[1]);
            String outname = outpath.getFileName().toString();
            String outdir =  outpath.getParent()!=null? outpath.getParent().toString():".";
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
            System.out.println("Working for JCC 2020");
            p.put(JConstants.MULTI_DATA, args[0].split("\\.")[0]);
            System.out.println(args[1]);

            //log contest
            PrintStream psout = new PrintStream(outpath.toString());
            PrintStream pslog;
            pslog = new PrintStream(outdir+sep+outname+".log");
            p.put(JConstants.LOG_FILE, pslog);
            pslog.println(Long.toString(System.currentTimeMillis()) + ":");
            pslog.println("null string");
            pslog.println("null distance");
            //endlog contest


            List<Example> iset = JUtils.loadExamples(inpath.toString());

           
            int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));

            MAJRStatisticalMultiOp m_js = new MAJRStatisticalMultiOp();
            MASet m_sm = new MASet();

            MAResult m_setMean = m_sm.getMean(iset, null, p);

            psout.println("SetMedian AvgDist: " + m_setMean.sumDist / iset.size() + " TotalDist: " + m_setMean.totalDist);
            psout.println(m_setMean.meanExample.toString());

            long start = System.currentTimeMillis();
            MAResult m_newMean = m_js.getMean(iset, m_setMean.meanExample, p);
            long end = System.currentTimeMillis();

            // nuevo 2
            float distancias2 = 0f;
            for (int i = 1; i < iset.size(); i++) {
                distancias2 += eD.dEdition(iset.get(i), m_newMean.meanExample, true).dist;
            }
            // end nuevo 2

            int totalDist = m_newMean.totalDist + m_setMean.totalDist;
            double median = distancias2 / iset.size();
            psout.println("Mean AvgDist: " + m_newMean.sumDist / iset.size() + " TotalDist: " + totalDist);
            psout.println(m_newMean.meanExample.toString());

            psout.println("PFO " + (end - start));
            for (String s : m_newMean.opPosList) {
                psout.println(s);
            }

            psout.close();
        } catch (Exception e) {
            for(StackTraceElement sse : e.getStackTrace()){
                System.err.println(sse.toString());
            }       
        }
    }
}

package strmean.malgorithms;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.StringJoiner;
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
 * problem described in: MOLLINEDA CÁRDENAS, R. A.. A learning model for
 * multiple-prototype classification of strings. 17th Int. Conf. on Pattern
 * Recognition (ICPR). 2004, pp. 420-423.
 */
public class MACardenas extends MAFischer2000 {

    int totalDist;
    public double c_minFreq;

    public MACardenas(Properties p) {
        super();
        c_minFreq = (double) p.get(JConstants.MIN_FREC);
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
    @Override
    protected List<Operation> selectOperations(OpStats opStats, int DBSize, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        String cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> comparator = JUtils.newInstance(Comparator.class, cmpType);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="initializations">
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

                if (tmp.opInfo.quality >= c_minFreq) {
                    selectedOps.add(tmp);
                }

            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="substitutions and deletions">
            if (!posOps[i].isEmpty()) {
                Collections.sort(posOps[i], comparator);
                tmp = (Operation) posOps[i].get(0);
                if (tmp.opInfo.quality >= c_minFreq) {
                    selectedOps.add(tmp);
                }
            }
            //</editor-fold> 
        }
        //</editor-fold>
        return selectedOps;
    }

    public static void main(String[] args) throws Exception {

        Path inpath = Paths.get(args[0]);
        Path outpath = Paths.get(args[1]);
        String inname = inpath.getFileName().toString();
        String outdir = outpath.getParent().toString();
        
        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        Properties p = JUtils.loadProperties();
        String sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif) JUtils.newInstance(SymbolDif.class, sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);

        String edType = p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        p.put(JConstants.MIN_FREC, 0.0d);
        //</editor-fold>


        PrintStream psout = new PrintStream(outpath.toString());
        
        String sep = System.getProperty("file.separator");
        PrintStream psE = new PrintStream(outdir+sep+inname + "_E_cardenas.csv");
        PrintStream psM = new PrintStream(outdir+sep+inname + "_M_cardenas.csv");

        List<Example> BD = JUtils.loadExamples(inpath.toString());

        double alpha = 0.1;
        StringJoiner labelE = new StringJoiner(",");
        StringJoiner avgDistE = new StringJoiner(",");
        StringJoiner distE = new StringJoiner(",");
        
        StringJoiner labelM= new StringJoiner(",");
        StringJoiner avgDistM = new StringJoiner(",");
        StringJoiner distM = new StringJoiner(",");
        
        labelE.add("alpha");
        avgDistE.add("avgDist");
        distE.add("totalDist");
        
        labelM.add("alpha");
        avgDistM.add("avgDist");
        distM.add("totalDist");
        
        int precision = Integer.parseInt(p.getProperty(JConstants.PRECISION, JConstants.DEFAULT_PRECISION));
                
        MACardenas js = new MACardenas(p);
        MASet sm = new MASet();
        
        MAResult setMean = sm.getMean(BD, null, p);
        double median = setMean.sumDist / BD.size();
        int totalDist = setMean.totalDist;
        double stdv = JMathUtils.round(JMathUtils.getStdv(setMean.distances, median), precision);
        psout.println("SetMedian AvgDist: " + median + " TotalDist: " + totalDist + " Stdv: " + stdv);
        psout.println(setMean.meanExample.toString());   
        
        
        MAResult bestE = null;
        MAResult bestM = null;
        double bestAE = 0.0;
        double bestAM = 0.0;
        
        while (alpha < 1) {
            js.c_minFreq = BD.size() * alpha;

            MAResult newMeanE = js.getMean(BD, new Example("-", ""), p);
            labelE.add(Double.toString(JMathUtils.round(alpha, 3)));
            avgDistE.add(Double.toString(newMeanE.sumDist / BD.size()));
            distE.add(Integer.toString(newMeanE.totalDist));
            

            MAResult newMeanM = js.getMean(BD, setMean.meanExample, p);
            labelM.add(Double.toString(JMathUtils.round(alpha, 3)));
            avgDistM.add(Double.toString(newMeanM.sumDist / BD.size()));
            distM.add(Integer.toString(setMean.totalDist+newMeanM.totalDist));

            if(bestE==null || bestE.sumDist>newMeanE.sumDist){
                bestE=newMeanE;
                bestAE = alpha;
            }
            if(bestM==null || bestM.sumDist>newMeanM.sumDist){
                bestM=newMeanM;
                bestAM = alpha;
            }
 
            alpha = alpha * 1.05;
        }


        median = bestE.sumDist / BD.size();
        totalDist = bestE.totalDist;
        stdv = JMathUtils.round(JMathUtils.getStdv(bestE.distances, median), precision);
        psout.println("Empty AvgDist: " + median + " TotalDist: " + totalDist + " Stdv: " + stdv + " alpha: " + bestAE);
        psout.println(bestE.meanExample.toString());   
        
        median = bestM.sumDist / BD.size();
        totalDist = setMean.totalDist + bestM.totalDist;
        stdv = JMathUtils.round(JMathUtils.getStdv(bestM.distances, median), precision);
        psout.println("Mean AvgDist: " + median + " TotalDist: " + totalDist + " AddDist: " + bestM.totalDist + " Stdv: " + stdv + " alpha: " + bestAM);
        psout.println(bestM.meanExample.toString());   
        
        psE.println(labelE);
        psE.println(avgDistE);
        psE.println(distE);

        psM.println(labelM);
        psM.println(avgDistM);
        psM.println(distM);
    }
}

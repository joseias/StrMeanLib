package strmean.malgorithms;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.data.Operation;
import strmean.data.SymbolDif;
import strmean.distances.EditDistance;
import strmean.main.JConstants;
import strmean.main.JMathUtils;
import strmean.main.JUtils;
import strmean.opstateval.OpStats;

public class MACardenas extends MAFischer2000 {

    int totalDist;
    public double c_minFreq;

    public MACardenas(Properties p) {
        super();
        c_minFreq = (double) p.get(JConstants.MIN_FREC);
    }

    /**
     * Dado el conjunto de todas las operaciones, devuelve la mejor en cada
     * posicion.
     *
     * @return
     */
    @Override
    protected List<Operation> selectOperations(OpStats opStats, int DBSize, Properties p) throws Exception {
        List<Operation> ops = opStats.getOperations();
        ArrayList[] posOps = new ArrayList[opStats.ex.sequence.length + 1];
        ArrayList[] posOpsI = new ArrayList[opStats.ex.sequence.length + 1];

        List<Operation> selectedOps = new ArrayList<>(opStats.ex.sequence.length);

        //<editor-fold defaultstate="collapsed" desc="Carga dinamica del comparador de operaciones">
        String cmpType = p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> comparator = JUtils.newInstance(Comparator.class, cmpType);
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Inicializar ">
        for (int i = 0; i < posOps.length; i++) {
            posOps[i] = new ArrayList<>();
            posOpsI[i] = new ArrayList<>();
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="Colocar cada operacion en la lista correspondiente a su posicion">
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

        //<editor-fold defaultstate="collapsed" desc="Ordenar las operaciones de cada posicion de acuerdo a su "goodness index" y seleccionarla">
        Operation tmp;
        for (int i = 0; i < posOps.length; i++) {
            //<editor-fold defaultstate="collapsed" desc="Procesar inserciones">
            if (!posOpsI[i].isEmpty()) {
                Collections.sort(posOpsI[i], comparator);
                tmp = (Operation) posOpsI[i].get(0);

                if (tmp.opInfo.quality >= c_minFreq) {
                    selectedOps.add(tmp);
                }

            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Procesar sustituciones y borrados">
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
        // PrintOperations(selectedOps);
        return selectedOps;
    }

    public static void main(String[] args) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p = JUtils.loadProperties();
        String sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif) JUtils.newInstance(SymbolDif.class, sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);

        String edType = p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        p.put(JConstants.MIN_FREC, 0.0d);
        //</editor-fold>

        String[] name = args[0].split("\\.");
        PrintStream psE = new PrintStream(name[0] + "_E_cardenas.out");
        PrintStream psM = new PrintStream(name[0] + "_cardenas.out");

        List<Example> BD = JUtils.loadExamples(args[0]);
        MACardenas js = new MACardenas(p);
        MASet sm = new MASet();

        double alpha = 0.1;
        String labelE = "";
        String avgDistE = "";
        String distE = "";

        String labelM = "";
        String avgDistM = "";
        String distM = "";

        while (alpha < 1) {
            js.c_minFreq = BD.size() * alpha;

            MAResult newMeanE = js.getMean(BD, new Example("0", ""), p);
            labelE = labelE + ",cardenasC1_E_" + JMathUtils.round(alpha, 3);
            avgDistE = avgDistE + "," + Double.toString(newMeanE.sumDist / BD.size());
            distE = distE + "," + newMeanE.totalDist;

            MAResult setMean = sm.getMean(BD, null, p);

            MAResult newMeanM = js.getMean(BD, setMean.meanExample, p);
            labelM = labelM + ",cardenasC1_" + JMathUtils.round(alpha, 3);
            avgDistM = avgDistM + "," + Double.toString(newMeanM.sumDist / BD.size());
            distM = distM + "," + newMeanM.totalDist;

            alpha = alpha * 1.05;

        }
        labelE = labelE.substring(1);
        avgDistE = avgDistE.substring(1);
        distE = distE.substring(1);

        labelM = labelM.substring(1);
        avgDistM = avgDistM.substring(1);
        distM = distM.substring(1);

        psE.println(labelE);
        psE.println(avgDistE);
        psE.println(distE);

        psM.println(labelM);
        psM.println(avgDistM);
        psM.println(distM);

    }

}

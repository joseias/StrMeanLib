package strmean.malgorithms;

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
import strmean.main.JUtils;
import strmean.opstateval.OpStats;

public class MAFischer2000 extends MAlgorithm {

    OpStats opStatsTemplate;

    @Override
    public MAResult getMean(List<Example> BD, Example seed, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        opStatsTemplate = JUtils.newInstance(OpStats.class, p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        //</editor-fold>
        boolean changed;
        Example actualCandidate = new Example(seed);
        Example bestExample = new Example(actualCandidate);
        List<Operation> ops;
        HashMap<String, Example> procExamples = new HashMap<>();

        OpStats opStatsCandidate;
        OpStats opStatsBestExample;

        int totalDist = 0;
        //<editor-fold defaultstate="collapsed" desc="Calcular distancias y estadisticas">
        opStatsCandidate = this.testExample(bestExample, BD, p);
        totalDist = totalDist + opStatsCandidate.totalDist;
        opStatsBestExample = opStatsCandidate;

        procExamples.put(new String(bestExample.sequence), bestExample);
        //</editor-fold>
        do {
            changed = false;

            //<editor-fold defaultstate="collapsed" desc="Obtener operaciones a aplicar">
            ops = this.selectOperations(opStatsCandidate, BD.size(), p);

            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Obtener nueva candidata">
            actualCandidate = bestExample.applyOperations(ops);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Probar candidata">
            String key = new String(actualCandidate.sequence);
            if (!procExamples.containsKey(key)) {
                /*Si esta en la tabla no deberia ser mejor que bestExample*/
                opStatsCandidate = this.testExample(actualCandidate, BD, p);
                totalDist = totalDist + opStatsCandidate.totalDist;

                procExamples.put(key, actualCandidate);

                if (opStatsBestExample.sumDist > opStatsCandidate.sumDist) {
                    bestExample = actualCandidate;
                    opStatsBestExample = opStatsCandidate;

                    changed = true;
                }
            }
            //</editor-fold>
        } while (changed == true);

        MAResult result = new MAResult();
        result.meanExample = bestExample;
        result.sumDist = opStatsBestExample.sumDist;
        result.totalDist = totalDist;

        return result;
    }

    private OpStats testExample(Example candidate, List<Example> BD, Properties p) {

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        OpStats opStats = opStatsTemplate.newInstance();
        opStats.init(candidate, ed._sd, BD.size());

        EDResult edR;

        for (Example e : BD) {
            edR = ed.dEdition(candidate, e, true);
            opStats.totalDist++;
            opStats.sumDist = opStats.sumDist + edR.dist;

            for (Operation op : edR.getOperations()) {
                opStats.addOperation(op);
            }
        }

        return opStats;
    }

    /**
     * Dado el conjunto de todas las operaciones, devuelve la mejor en cada
     * posicion.
     *
     * @param opStats
     * @param DBSize
     * @param p
     * @return
     * @throws java.lang.Exception
     */
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
                /*Esto es para que quede igual que la implementacion de JMediaCadenas_20120926...*/
                if (tmp.opInfo.quality >= DBSize / 2) {
                    selectedOps.add(tmp);
                }
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Procesar sustituciones y borrados">
            if (!posOps[i].isEmpty()) /*Deberia ocurrir siempre...salvo quizas en la ultima posicion*/ {
                Collections.sort(posOps[i], comparator);
                tmp = (Operation) posOps[i].get(0);
                selectedOps.add(tmp);
            }
            //</editor-fold> 
        }
        //</editor-fold>
        // printOperations(selectedOps);
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

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p = JUtils.loadProperties();
        String sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = JUtils.newInstance(SymbolDif.class, sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);

        String edType = p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>

//        String cmpType=JUtils.getArgsType(JConstants.PROPERTIES_FILE, "symbolDif");
//        SymbolDif sd=(SymbolDif)JUtils.buildInstanceFromType(cmpType);
        List<Example> BD = JUtils.loadExamples(args[0]);
        MAFischer2000 js = new MAFischer2000();
        MASet sm = new MASet();

        MAResult newMeanE = js.getMean(BD, new Example("0", ""), p);
        //Example newMeanE=js.getMean(BD,BD.get(0));

        System.out.println("Empty AvgDist: " + newMeanE.sumDist / BD.size() + " TotalDist: " + newMeanE.totalDist);
        System.out.println(newMeanE.meanExample.toString());

        MAResult setMean = sm.getMean(BD, null, p);
        System.out.println("SetMedian AvgDist: " + setMean.sumDist / BD.size() + " TotalDist: " + (setMean.totalDist));
        System.out.println(setMean.meanExample.toString());

        MAResult newMean = js.getMean(BD, setMean.meanExample, p);
        System.out.println("Mean AvgDist: " + newMean.sumDist / BD.size() + " TotalDist: " + (setMean.totalDist + newMean.totalDist));
        System.out.println(newMean.meanExample.toString());

    }

}

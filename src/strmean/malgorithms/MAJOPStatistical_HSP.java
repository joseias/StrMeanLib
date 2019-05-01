/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor esteeeeeeee.
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
 * Esto es una modificación de MAJRStatistical
 * @authores jabreu, pedroasm
 */
public class MAJOPStatistical_HSP extends MAlgorithm
{
    OpStats opStatsTemplate;
        
    @Override
    public MAResult getMean(List<Example> a_BD, Example a_seed,Properties p) throws Exception
    {
        boolean m_changed;
        boolean keepTryingOps;
        int precision=Integer.parseInt(p.getProperty(JConstants.PRECISION,JConstants.DEFAULT_PRECISION));
        
        Example m_actualCandidate=new Example(a_seed);
        Example m_bestExample=new Example(m_actualCandidate);
        
        List<Operation> m_ops;
        List<Operation> m_opsTmp;
        List<String> opPosL=new LinkedList<>();
        
        HashMap<String,Example>  m_procExamples=new HashMap<>();
        
        OpStats opStatsBestExample;
        opStatsTemplate= (OpStats)JUtils.buildInstanceFromType(p.getProperty(JConstants.OPS_STAT_EVALUATOR));
                
        Operation op;
        
        long cEpoch=0;
        long maxEpoch=Long.parseLong(p.getProperty(JConstants.MAX_EPOCH,"0"));
        
        int opPos;
        int maxOps=Integer.parseInt(p.getProperty(JConstants.MAX_OPS,"0"));
        int cOps;
        int totalDist=0;
        
        //<editor-fold defaultstate="collapsed" desc="Carga dinamica del comparador de operaciones">
        String m_cmpType=p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> m_comparator=(Comparator<Operation>)JUtils.buildInstanceFromType(m_cmpType);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Calcular distancias y estadisticas">
        OpStats opStatsCandidate=this.testExample(m_bestExample,a_BD ,Float.MAX_VALUE, p);
        totalDist=totalDist+opStatsCandidate.totalDist;
        
        opStatsBestExample=opStatsCandidate;
        
        m_procExamples.put(new String(m_bestExample.sequence), m_bestExample);
        //</editor-fold>
        do
        {
//            System.err.println(epoch);
            cEpoch++;
            m_changed=false;
         
            //<editor-fold defaultstate="collapsed" desc="Obtener operaciones y ordenar de acuerdo al criterio dado">
            m_ops=opStatsCandidate.getOperations();
            
            //<editor-fold defaultstate="collapsed" desc="eliminar opp con quality menor que 0">
            //modificado 171114 eliminar opp con quality < 0
            
            int ip = 0;
            while (ip < m_ops.size()) {                
                if (m_ops.get(ip).opInfo.quality <= 0) {
                    m_ops.remove(ip);
                }
                else {
                    ip++;
                }
            }
            //</editor-fold>
            Collections.sort(m_ops, m_comparator);    
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Probar las operaciones de acuerdo a su ranking">
            
            Iterator<Operation> itOps=m_ops.iterator();
            cOps=0;
            opPos=1;
            keepTryingOps=true;
            
            while(itOps.hasNext() && keepTryingOps==true){
                op=itOps.next();
                
                if(op.type!='s' || op.a!=op.b) /*Si no es una sustitucion de un simbolo por si mismo*/
                {
                    //<editor-fold defaultstate="collapsed" desc="Obtener nueva candidata">
                    m_opsTmp=new ArrayList<>(1);
                    m_opsTmp.add(op);
                    m_actualCandidate=m_bestExample.applyOperations(m_opsTmp);
                    
                    //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Probar candidata">
                    String key=new String(m_actualCandidate.sequence);
                    if(!m_procExamples.containsKey(key))
                    {
                        /*Si est\'{a} en la tabla no deber\'{i}a ser mejor que bestExample*/
                        opStatsCandidate=this.testExample(m_actualCandidate, a_BD, opStatsBestExample.sumDist, p);
                        totalDist=totalDist+opStatsCandidate.totalDist;
                        
                        m_procExamples.put(key,m_actualCandidate);
                        
                        if(opStatsBestExample.sumDist>opStatsCandidate.sumDist)
                        {
                            double meanOld=JMathUtils.round(opStatsBestExample.sumDist/a_BD.size(), precision);
                            double meanNew=JMathUtils.round(opStatsCandidate.sumDist/a_BD.size(), precision);
                            double stdvNew=JMathUtils.round(JMathUtils.getStdv(opStatsCandidate.distances, meanNew), precision);
       
                            double expectedV=JMathUtils.round(op.opInfo.quality/a_BD.size(), precision);
                            double deltha=JMathUtils.round((meanOld-meanNew), precision);
                            
                            m_bestExample=m_actualCandidate;
                            opStatsBestExample=opStatsCandidate;
                            m_changed=true;
                            
                            String info=meanOld+" "+meanNew+" "+deltha+" "+expectedV+" "+stdvNew+" "+totalDist; 
                            opPosL.add(Integer.toString(opPos)+"-"+op.toString()+" "+info);

                        }
                        /*JComment: Patch to print the  operation if maxEpoch==1*/
                        else{
                            if(maxEpoch==1){
                                opPosL.add(Integer.toString(opPos)+"-"+op.toString());
                            }
                        }
                    }     
                    //</editor-fold>
                    opPos++;
                    cOps++; 
                }  
                
                keepTryingOps=!m_changed && ((maxOps<=0) || (maxOps>0 && cOps<maxOps)); 
            }
            //</editor-fold>
        }
        /*Continue if there was an improvement, or max Epoch are reached, if specified*/
        while(m_changed==true && ((maxEpoch<=0) || (maxEpoch>0 && cEpoch<maxEpoch)));
        
        MAResult result=new MAResult();
        result.meanExample=m_bestExample;
        result.sumDist=opStatsBestExample.sumDist;
        result.totalDist=totalDist;
        result.opPosList=opPosL;
        result.distances=opStatsBestExample.distances;
        return result; 
      
    }
    
    /***
     *  Warning: This method relies in the previous initialization of opStatsTemplate
     *  this is to avoid repeated creations of OpStats objects by reflection...
     * @param a_candidate
     * @param a_BD
     * @param thresholdDist
     * @param p
     * @return 
     */
    protected OpStats testExample(Example a_candidate,List<Example> a_BD,float thresholdDist,Properties p){
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>
        OpStats opStats=opStatsTemplate.newInstance();
        opStats.init(a_candidate,ed._sd,a_BD.size());
        
        EDResult edR;
        int index=0;
        for(Example e:a_BD)
        {
//            System.out.println(e.toString());
            edR=ed.dEdition(a_candidate, e, true);
            opStats.totalDist++;
            opStats.sumDist=opStats.sumDist+edR.dist;
            opStats.distances[index]=edR.dist;
	    index++;
            
            /*Optimization to speed up the test*/
            if(opStats.sumDist>thresholdDist){
                /*Abort execution*/
                return opStats;
            }
            
            for(Operation op:edR.getOperations()){
                opStats.addOperation(op);
            }         
        }
        
        return opStats;
    }
    
    public static void main(String[] args) throws Exception
    {
        JUtils.initLogger();
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>
        
        System.out.println("Working 171219 HSP");
        
        PrintStream pslog=new PrintStream(args[1]+".log");
        p.put(JConstants.LOG_FILE, pslog);
        
        List<Example> lista=JUtils.loadExamples(args[0]);
        
        //HSP
        int distanciasCalculadas = (lista.size()*(lista.size()-1))/2; //este es el valor necesario para calcular la mediana
        int tamanoLista = lista.size();
        float[][] matrizDistancias = new float[tamanoLista][tamanoLista +3 ];
        for (int i = 0; i < tamanoLista; i++) { //inicializando valores para calcular el mínimo
            matrizDistancias[i][tamanoLista + 1]=Float.MAX_VALUE;
        }
        for (int i = 0; i < matrizDistancias.length; i++) {
            for (int j = 0; j < matrizDistancias.length; j++) {
                
                if (i!=j) {
                    float temp = eD.dEdition(lista.get(i), lista.get(j), false).dist;
                    matrizDistancias[i][j]=temp;
                    matrizDistancias[i][tamanoLista]+=temp;
                    if (temp < matrizDistancias[i][tamanoLista + 1] ) {
                        matrizDistancias[i][tamanoLista + 1] = temp;
                        matrizDistancias[i][tamanoLista + 2] = j;
                    }
                }
            }
        }
        float menosDistancias = matrizDistancias[0][tamanoLista];
        int indiceMenosDistancias =  Math.round(matrizDistancias[0][tamanoLista + 2]);
        for (int i = 1; i < tamanoLista; i++) {
            if (matrizDistancias[i][tamanoLista] < menosDistancias) {
                menosDistancias = Math.round(matrizDistancias[i][tamanoLista]);
                indiceMenosDistancias = i;
            }
        }
        boolean noPendientes[] = new boolean[tamanoLista];
        noPendientes[indiceMenosDistancias] = true;
        noPendientes[Math.round(matrizDistancias[indiceMenosDistancias][tamanoLista + 2])]   = true;
        int elementosResueltos = 2;
        int masCercano = Math.round(matrizDistancias[indiceMenosDistancias][tamanoLista + 2]);
        
        //calculando pivotes según HSP
        List<List<Integer>> pivotes;
        pivotes = new LinkedList<>();
        pivotes.add(new LinkedList<Integer>());
        pivotes.get(pivotes.size()-1).add(masCercano);
        
        do { 
            for (int i = 0; i < tamanoLista; i++) {
                if (!noPendientes[i]) {
                    if (matrizDistancias[masCercano][i] <= matrizDistancias[indiceMenosDistancias][i]) {
                        if (i != masCercano) {
                            pivotes.get(pivotes.size()-1).add(i);
                            elementosResueltos ++;
                            noPendientes[i] = true;
                        }

                }
                }
                
            }
        //buscando nuevo pivote
            if (elementosResueltos < tamanoLista) {
                float pivoteCandidatoValor = Float.MAX_VALUE;
                int pivoteCandidato = -1;
                for (int i = 0; i < tamanoLista; i++) {
                    if (!noPendientes[i]) {
                        if (matrizDistancias[i][indiceMenosDistancias] < pivoteCandidatoValor) {
                            pivoteCandidatoValor = matrizDistancias[i][indiceMenosDistancias];
                            pivoteCandidato = i;
                        }
                    }
                }
                elementosResueltos ++;
                noPendientes[pivoteCandidato] = true;
                pivotes.add(new LinkedList<Integer>());
                pivotes.get(pivotes.size()-1).add(pivoteCandidato);
                masCercano = pivoteCandidato;
            }
                
        } while (elementosResueltos<tamanoLista);
        
        //reasignando elementos a los pivotes obtenidos
        
        List<List<Integer>> nuevosPivotes = new LinkedList<>();
        for (int i = 0; i < pivotes.size(); i++) {
            nuevosPivotes.add(new LinkedList<Integer>());
            nuevosPivotes.get(nuevosPivotes.size()-1).add(pivotes.get(i).get(0));
        }
        
        for (int i = 0; i < tamanoLista; i++) {
            if (i != indiceMenosDistancias) {
                float minimoDistancia = matrizDistancias[i][pivotes.get(0).get(0)];
                int indice = 0;
                for (int j = 1; j < pivotes.size(); j++) {
                    if (matrizDistancias[i][pivotes.get(j).get(0)] < minimoDistancia) {
                        minimoDistancia = matrizDistancias[i][pivotes.get(j).get(0)];
                        indice = j;
                    }
                }
                if (nuevosPivotes.get(indice).get(0) != i) {
                    nuevosPivotes.get(indice).add(i);
                }
                
            }
        }
        
        // a partir de args[2] se recalculan algunos centroides de los pivotes
        // all -> todos
        // none -> ninguno
        // bigs -> los que representen más que la porción que el correspondiera
        //         si estuvueran repartidos equitativamente
        List<Example> ejemplosPivotes=new LinkedList<>();
        List<Integer> ejemplosPivotesPesos = new ArrayList<Integer>();
        ejemplosPivotes.add(lista.get(indiceMenosDistancias));
        ejemplosPivotesPesos.add(1);
        for (int i = 0; i < nuevosPivotes.size(); i++) {
            if (args[2].equals("all")) {
                //calculando centroide de pivote
                int tamanoPivote =  nuevosPivotes.get(i).size();
                System.out.println("Calculando Centroide para Pivote " + (i+1) + " de " + nuevosPivotes.size() + " con " + nuevosPivotes.get(i).size() + " cadenas");
                List<Example> subPivotes=new LinkedList<>();
                List<Integer> subPivotesPesos = new ArrayList<Integer>();
                for (int j = 0; j < nuevosPivotes.get(i).size(); j++) {
                    subPivotes.add(lista.get(nuevosPivotes.get(i).get(j)));
                    subPivotesPesos.add(1);
                }
                MASet m_1=new MASet();
                MAResult paraMediana=m_1.getMean(lista,null, p);
                MAlgorithm m_sm=new MAJRStatistical();
                MAResult m_setMean=m_sm.getMean(subPivotes, paraMediana.meanExample, p);
                ejemplosPivotes.add(m_setMean.meanExample);
                ejemplosPivotesPesos.add(nuevosPivotes.get(i).size());
                distanciasCalculadas += paraMediana.totalDist + m_setMean.totalDist;
            }
            else {
                if (args[2].equals("big")) {
                    //calculando centroide de pivote
                    List<Example> subPivotes=new LinkedList<>();
                    List<Integer> subPivotesPesos = new ArrayList<Integer>();
                    System.out.println("Calculando Centroide para Pivote " + (i+1) + " de " + nuevosPivotes.size() + " con " + nuevosPivotes.get(i).size() + " cadenas");
                    if (nuevosPivotes.get(i).size() > lista.size()/nuevosPivotes.size()) {
                        for (int j = 0; j < nuevosPivotes.get(i).size(); j++) {
                            subPivotes.add(lista.get(nuevosPivotes.get(i).get(j)));
                            subPivotesPesos.add(1);
                        }
                        MASet m_1=new MASet();
                        MAResult paraMediana=m_1.getMean(lista,null, p);
                        MAlgorithm m_sm=new MAJRStatistical();
                        MAResult m_setMean=m_sm.getMean(subPivotes, paraMediana.meanExample, p);
                        ejemplosPivotes.add(m_setMean.meanExample);
                        ejemplosPivotesPesos.add(nuevosPivotes.get(i).size());
                        distanciasCalculadas += paraMediana.totalDist + m_setMean.totalDist;
                    }
                    else{
                        ejemplosPivotes.add(lista.get(nuevosPivotes.get(i).get(0)));
                        ejemplosPivotesPesos.add(nuevosPivotes.get(i).size());
                    }  
                }
                else{
                    ejemplosPivotes.add(lista.get(nuevosPivotes.get(i).get(0))  );
                    ejemplosPivotesPesos.add(nuevosPivotes.get(i).size());
                }  
            }  
        }
        
        
        
        System.out.println("terminado el cálculo de pivotes HSP");
            
        PrintStream ps=new PrintStream(args[1]);
        int precision=Integer.parseInt(p.getProperty(JConstants.PRECISION,JConstants.DEFAULT_PRECISION));
        
        //arreglar los cálculos
        ps.println("SetMedian AvgDist: "+ menosDistancias/lista.size() +" TotalDist: "+ distanciasCalculadas);
        ps.println(ejemplosPivotes.get(0).sequence);
        
        System.out.println("viene lo bueno...");
        
        //ver desde aquí
        MAlgorithm m_sm=new MAJRStatistical();
        for(int i=0;i<ejemplosPivotes.size(); i++){
            ejemplosPivotes.get(i).weigth = ejemplosPivotesPesos.get(i);
        }
        
        MAResult m_setMean=m_sm.getMean(ejemplosPivotes, ejemplosPivotes.get(0), p);
        
        // nuevo 2
        float distancias2 = 0f;
        for (int i = 1; i < lista.size(); i++)
        {
            distancias2 += eD.dEdition(lista.get(i), m_setMean.meanExample, true).dist;
        }
        // fin nuevo 2
        
        int totalDist=m_setMean.totalDist + distanciasCalculadas;
        double median=distancias2/lista.size();
        ps.println("Mean AvgDist: "+median+" TotalDist: "+ totalDist );
        ps.println(m_setMean.meanExample.toString());
        System.out.println("Mean AvgDist: "+median+" TotalDist: "+totalDist);

        for(String s:m_setMean.opPosList){
            ps.println(s);
        }

        ps.close();
        System.out.println("Todo ok...");
//        System.in.read();
    }
}


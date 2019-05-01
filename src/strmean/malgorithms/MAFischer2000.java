/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author jabreu
 */
public class MAFischer2000 extends MAlgorithm
{
    OpStats opStatsTemplate;
            
    @Override
    public MAResult getMean(List<Example> a_BD, Example a_seed,Properties p) throws Exception
    {
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        opStatsTemplate=(OpStats)JUtils.buildInstanceFromType(p.getProperty(JConstants.OPS_STAT_EVALUATOR));
        //</editor-fold>
        boolean m_changed;
        Example m_actualCandidate=new Example(a_seed);
        Example m_bestExample=new Example(m_actualCandidate);
        List<Operation> m_ops;
        HashMap<String,Example>  m_procExamples=new HashMap<>();
        
        OpStats opStatsCandidate;
        OpStats opStatsBestExample;

                
        int totalDist=0;
        //<editor-fold defaultstate="collapsed" desc="Calcular distancias y estadisticas">
        opStatsCandidate=this.testExample(m_bestExample, a_BD, p);
        totalDist=totalDist+opStatsCandidate.totalDist;
        opStatsBestExample=opStatsCandidate;
        
        m_procExamples.put(new String(m_bestExample.sequence), m_bestExample);
        //</editor-fold>
        do
        {
            m_changed=false;
            
            //<editor-fold defaultstate="collapsed" desc="Obtener operaciones a aplicar">
            m_ops=this.selectOperations(opStatsCandidate,a_BD.size(), p);
   
            //</editor-fold>
 
            //<editor-fold defaultstate="collapsed" desc="Obtener nueva candidata">
            m_actualCandidate=m_bestExample.applyOperations(m_ops);
            //</editor-fold>
            
            //<editor-fold defaultstate="collapsed" desc="Probar candidata">
            String key=new String(m_actualCandidate.sequence);
            if(!m_procExamples.containsKey(key))
            {
                /*Si esta en la tabla no deberia ser mejor que bestExample*/
                opStatsCandidate = this.testExample(m_actualCandidate, a_BD, p);
                totalDist=totalDist+opStatsCandidate.totalDist;
                
                m_procExamples.put(key,m_actualCandidate);

                if(opStatsBestExample.sumDist>opStatsCandidate.sumDist)
                {
                    m_bestExample=m_actualCandidate;
                    opStatsBestExample=opStatsCandidate;
                    
                    m_changed=true;
                }         
            }     
            //</editor-fold>
        }
        while(m_changed==true);
        
        MAResult result=new MAResult();
        result.meanExample=m_bestExample;
        result.sumDist=opStatsBestExample.sumDist;
        result.totalDist=totalDist;

        return result;    
    }
    
    private OpStats testExample(Example a_candidate,List<Example> a_BD,Properties p)
    {
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>
        
        
        OpStats opStats=opStatsTemplate.newInstance();
        opStats.init(a_candidate, ed._sd,a_BD.size());
        
        EDResult edR;
        
        for(Example e:a_BD)
        {
            edR=ed.dEdition(a_candidate, e, true);
            opStats.totalDist++;
            opStats.sumDist=opStats.sumDist+edR.dist;
            
            for(Operation op:edR.getOperations()){
                opStats.addOperation(op);
            }
        }
        
        return opStats;
    }
    
    /**
     * Dado el conjunto de todas las operaciones, devuelve la mejor en cada posicion.
     * @param opStats
     * @param a_DBSize
     * @return 
     * @throws java.lang.Exception 
     */
    protected List<Operation> selectOperations(OpStats opStats,int a_DBSize, Properties p) throws Exception
    {
        List<Operation> m_ops = opStats.getOperations();
        
        ArrayList[] m_posOps=new ArrayList[opStats.ex.sequence.length+1];
        ArrayList[] m_posOpsI=new ArrayList[opStats.ex.sequence.length+1];
        
        List<Operation> m_selectedOps=new ArrayList<>(opStats.ex.sequence.length);
        
        //<editor-fold defaultstate="collapsed" desc="Carga dinamica del comparador de operaciones">
        String m_cmpType=p.getProperty(JConstants.COMPARATOR_OPS);
        Comparator<Operation> m_comparator=(Comparator<Operation>)JUtils.buildInstanceFromType(m_cmpType);
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Inicializar ">
        for(int i=0;i<m_posOps.length;i++)
        {
            m_posOps[i]=new ArrayList<>();
            m_posOpsI[i]=new ArrayList<>();
        }       
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Colocar cada operacion en la lista correspondiente a su posicion">
        int m_pos;
        
        for(Operation op:m_ops)
        {
            m_pos=op.posSource;
            if(op.type=='i')
            {
                m_posOpsI[m_pos].add(op);
            }
            else
            {
                m_posOps[m_pos].add(op);
            }
            
        }      
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Ordenar las operaciones de cada posicion de acuerdo a su "goodness index" y seleccionarla">
        Operation m_tmp;
        for(int i=0;i<m_posOps.length;i++)
        {
            //<editor-fold defaultstate="collapsed" desc="Procesar inserciones">
             if(!m_posOpsI[i].isEmpty())
            {   
                Collections.sort(m_posOpsI[i],m_comparator);
                m_tmp=(Operation)m_posOpsI[i].get(0);
                /*Esto es para que quede igual que la implementacion de JMediaCadenas_20120926...*/
                if(m_tmp.opInfo.quality>=a_DBSize/2)
                {
                    m_selectedOps.add(m_tmp);
                }
            }
            //</editor-fold>
           
            //<editor-fold defaultstate="collapsed" desc="Procesar sustituciones y borrados">
            if(!m_posOps[i].isEmpty()) /*Deberia ocurrir siempre...salvo quizas en la ultima posicion*/
            {
                Collections.sort(m_posOps[i],m_comparator);
                m_tmp=(Operation)m_posOps[i].get(0);
                m_selectedOps.add(m_tmp);
            }
             //</editor-fold> 
        }
        //</editor-fold>
       // printOperations(m_selectedOps);
        return m_selectedOps;
    }
    
    private void printOperations(ArrayList<Operation> a_ops)
    {
        for(Operation op:a_ops)
        {
            char a=' ';
            char b=' ';
            
            switch(op.type)
            {
                case 's':
                        a=op.a;
                        b=op.b;
                        break;
                case 'i':
                        a='e';
                        b=op.b;
                        break;
                case 'd':
                        a=op.a;
                        b='e';
                        break;
                        
            }
            String m_v=Float.toString(op.opInfo.quality);
            switch(m_v.length())
            {
                case 1:
                    m_v="00".concat(m_v);
                    break;
                case 2:
                    m_v="0".concat(m_v);
                    break;
            }
            System.out.print(op.type+":"+a+":"+b+":"+op.posSource+":"+m_v+" " );
        }
        System.out.println();
    
    }
   
    public static void main(String[] args) throws Exception
    {
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>

                
//        String m_cmpType=JUtils.getArgsType(JConstants.PROPERTIES_FILE, "symbolDif");
//        SymbolDif a_sd=(SymbolDif)JUtils.buildInstanceFromType(m_cmpType);
        
        
        List<Example> m_BD=JUtils.loadExamples(args[0]);
        MAFischer2000 m_js=new MAFischer2000();
        MASet m_sm=new MASet();
        
        MAResult m_newMeanE=m_js.getMean(m_BD,new Example("0",""), p);
        //Example m_newMeanE=m_js.getMean(m_BD,m_BD.get(0));
        
        System.out.println("Empty AvgDist: "+m_newMeanE.sumDist/m_BD.size()+" TotalDist: "+m_newMeanE.totalDist);
        System.out.println(m_newMeanE.meanExample.toString());
 
        MAResult m_setMean=m_sm.getMean(m_BD,null, p);
        System.out.println("SetMedian AvgDist: "+m_setMean.sumDist/m_BD.size()+" TotalDist: "+(m_setMean.totalDist));
        System.out.println(m_setMean.meanExample.toString());
        
        
        MAResult m_newMean=m_js.getMean(m_BD,m_setMean.meanExample, p);
        System.out.println("Mean AvgDist: "+m_newMean.sumDist/m_BD.size()+" TotalDist: "+(m_setMean.totalDist+m_newMean.totalDist));
        System.out.println(m_newMean.meanExample.toString());
        
    }
    
}

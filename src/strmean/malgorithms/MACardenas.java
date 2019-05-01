/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author jabreu
 */
public class MACardenas extends MAFischer2000
{
    int totalDist;
    public double c_minFreq ;
    public MACardenas(Properties p)
    {
        super();
        c_minFreq = (double) p.get(JConstants.MIN_FREC);
    }
        
    
    /**
     * Dado el conjunto de todas las operaciones, devuelve la mejor en cada posicion.
     * @return 
     */
    @Override
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

                if(m_tmp.opInfo.quality>=c_minFreq)
                {
                    m_selectedOps.add(m_tmp);
                }
 
            }
            //</editor-fold>
           
            //<editor-fold defaultstate="collapsed" desc="Procesar sustituciones y borrados">
            if(!m_posOps[i].isEmpty()) 
            {
                Collections.sort(m_posOps[i],m_comparator);
                m_tmp=(Operation)m_posOps[i].get(0);
                if(m_tmp.opInfo.quality>=c_minFreq)
                {
                    m_selectedOps.add(m_tmp);
                }
            }
             //</editor-fold> 
        }
        //</editor-fold>
       // PrintOperations(m_selectedOps);
        return m_selectedOps;
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
        p.put(JConstants.MIN_FREC, 0.0d);
        //</editor-fold>

        String[] m_name=args[0].split("\\.");
        PrintStream m_psE=new PrintStream(m_name[0]+"_E_cardenas.out");
        PrintStream m_psM=new PrintStream(m_name[0]+"_M_cardenas.out");
        
        
        List<Example> m_BD=JUtils.loadExamples(args[0]); 
        MACardenas m_js=new MACardenas(p);
        MASet m_sm=new MASet();
        
        double alpha=0.1;
        String m_labelE="";
        String m_avgDistE="";
        String m_distE="";
        
        String m_labelM="";
        String m_avgDistM="";
        String m_distM="";
        
        
        while (alpha<1)
        {
            m_js.c_minFreq=m_BD.size()*alpha;
            
            MAResult m_newMeanE=m_js.getMean(m_BD,new Example("0",""),p);
            m_labelE=m_labelE+",cardenasC1_E_"+JMathUtils.round(alpha, 3);
            m_avgDistE=m_avgDistE+","+Double.toString(m_newMeanE.sumDist/m_BD.size());
            m_distE=m_distE+","+m_newMeanE.totalDist;
 
            MAResult m_setMean=m_sm.getMean(m_BD,null, p);
            
            MAResult m_newMeanM=m_js.getMean(m_BD,m_setMean.meanExample,p);
            m_labelM=m_labelM+",cardenasC1_M_"+JMathUtils.round(alpha, 3);
            m_avgDistM=m_avgDistM+","+Double.toString(m_newMeanM.sumDist/m_BD.size());
            m_distM=m_distM+","+m_newMeanM.totalDist;
            
            alpha=alpha*1.05;

        }
        m_labelE=m_labelE.substring(1);
        m_avgDistE=m_avgDistE.substring(1);
        m_distE=m_distE.substring(1);
        
        m_labelM=m_labelM.substring(1);
        m_avgDistM=m_avgDistM.substring(1);
        m_distM=m_distM.substring(1);
        
        m_psE.println(m_labelE);
        m_psE.println(m_avgDistE);
        m_psE.println(m_distE);
        
        m_psM.println(m_labelM);
        m_psM.println(m_avgDistM);
        m_psM.println(m_distM);
         
    }
    

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.main;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import strmean.malgorithms.MAJRStatistical;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.data.Operation;
import strmean.data.SymbolDif;
import strmean.distances.EditDistance;

/**
 *
 * @author sijfg
 */
public class Main {
    public static void main(String[] args)throws Exception{
       
        testEDL();
    }
    
    public static void validateExample(String[] args) throws Exception{
        System.out.println("Working...");
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>

        List<Example> m_BD=JUtils.loadExamples(args[0]);
        Example candidateMean=new Example("A","3333445455554554555555555554545456556765555565556555667777011111111111211007777777777776665566655656676777011111111212122121121122121212122121221122222232");
        
        double totalDist=0;
        for(Example e:m_BD){
            totalDist=totalDist+eD.dEdition(candidateMean, e, false).dist;
        }
        totalDist=totalDist/m_BD.size();
        
        System.out.println("AvgDist: "+totalDist);
        
    }
    
    public static void testEDDLRestricted ()throws Exception{
               
        System.out.println("Working...");
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>
        
//        float costo = dm.dEdition("ba", "ab");
//        System.out.println(costo);
//        
        Example ex=new Example("F","3333333434655666677677770777777777777676555555543323332323343333345546676777777776777777766555565566565556656777700112111111112112211111122121111121121123223333323333333332333");
        Example ey=new Example("F","433332333333333343333343344554556677777700777777777677665555556656555433334323333333343454555676777777777777777776655555555655556555677777011111112111211112011211111111112111112111112112112121");
        
//        Example ex=new Example("A","rabxdc");
//        Example ey=new Example("A","fbaxcdt");
        
//        Example ex=new Example("A","aa");
//        Example ey=new Example("A","bbbbb");
        
        EDResult dlr= eD.dEdition(ex, ey,true);
        System.out.println(dlr.dist);
        
//        Collections.sort(dlr.getOperations(),new ComparatorOperationPos());
        for (Iterator<Operation> it = dlr.getOperations().iterator(); it.hasNext();) {
            Operation o = it.next();
            System.out.println(o.toString());
        }
        
        
        Example nE = ex.applyOperations(dlr.getOperations());
        System.out.println(nE.toString());
    }

    public static void testMJRStatistical(String[] args)throws Exception{
        System.out.println("Working...");
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>
        
        List<Example> m_BD=JUtils.loadExamples(args[0]); 
        
        MAJRStatistical m_js=new MAJRStatistical();
        
        /*Values for total dist and set median were previously computed to speed up the debug*/
        Example setMeanF=new Example("F","3333333434655666677677778777777777777676555555543323332323343333345546676777777776777777766555565566565556656777788112111111112112211111122121111121121123223333323333333332333");
 
        MAResult m_newMean=m_js.getMean(m_BD,setMeanF,p);
        int totalDist=m_newMean.totalDist+142506;
        System.out.println("Mean AvgDist: "+m_newMean.sumDist/m_BD.size()+" TotalDist: "+totalDist);
        System.out.println(m_newMean.meanExample.toString());
        

//        System.out.println("Press Enter to exit...");
//        System.in.read();
    }

    public static void testEDL(){
                   
        System.out.println("Working...");
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        Properties p=JUtils.loadProperties();
        String m_sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = (SymbolDif)JUtils.buildInstanceFromType(m_sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);
        
        String m_edType=p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD=(EditDistance)JUtils.buildInstanceFromType(m_edType,p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>
        
//        float costo = dm.dEdition("ba", "ab");
//        System.out.println(costo);
//        
        Example ex=new Example("F","aa");
        Example ey=new Example("F","bacfffffade");
        
//        Example ex=new Example("A","rabxdc");
//        Example ey=new Example("A","fbaxcdt");
        
//        Example ex=new Example("A","aa");
//        Example ey=new Example("A","bbbbb");
        
        EDResult dlr= eD.dEdition(ex, ey,true);
        System.out.println(dlr.dist);
        
//        Collections.sort(dlr.getOperations(),new ComparatorOperationPos());
        for (Operation o : dlr.getOperations()) {
            System.out.println(o.toString());
        }
        
        
        Example nE = ex.applyOperations(dlr.getOperations());
        System.out.println(nE.toString());
    }
}

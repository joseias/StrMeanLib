/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.malgorithms;

import java.util.List;
import java.util.Properties;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.data.SymbolDif;
import strmean.distances.EditDistance;
import strmean.main.JConstants;
import strmean.main.JUtils;

/**
 *
 * @author jabreu
 */
public class MASet extends MAlgorithm
{
    
    /***
     * Requires a properties object with:
     *  - EditDistance object, mapped with key JConstants.EDIT_DISTANCE
     * @param a_BD
     * @param a_seed
     * @param p
     * @return
     * @throws Exception 
     */
    @Override
    public MAResult getMean(List<Example> a_BD, Example a_seed,Properties p) throws Exception
    {    
        
        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>
        
        float m_betterSum=Float.MAX_VALUE;
         int m_betterIndex=-1;
         float tmpDist;
         int totalDist=0;
         int dbSize=a_BD.size();
         Example source;

         float[] sumDist=new float[a_BD.size()];
         
         for(int i=0;i<dbSize;i++)
         {
             source=a_BD.get(i);
             for(int j=i+1;j<dbSize;j++)
             {
                tmpDist=ed.dEdition(source,a_BD.get(j), false).dist;
                totalDist++;
                sumDist[i]=sumDist[i]+tmpDist;
                sumDist[j]=sumDist[j]+tmpDist;
             }
             
             if(sumDist[i]<m_betterSum)
             {
                 m_betterIndex=i;
                 m_betterSum=sumDist[i];
             }
         }
         
         MAResult result=new MAResult();
         result.sumDist=m_betterSum;
         result.meanExample=a_BD.get(m_betterIndex);
         result.totalDist=totalDist;
         return result;
     }
    
    
    public static void main(String[] args) throws Exception
    {
        
//        String m_cmpType=JUtils.GetArgsType(JConstants.PROPERTIES_FILE, "symbolDif");
//        SymbolDif a_sd=(SymbolDif)JUtils.BuildInstance(m_cmpType);
//        
//        ArrayList<Example> m_BD=JUtils.LoadExamples(args[0]);
//        MASet m_js=new MASet(new EDLevenshtein());
//        
//        Example m_newMeanE=m_js.getMean(m_BD,new Example("0","",a_sd));
//        System.out.println("SetMean AvgDist: "+m_newMeanE.sumDist/m_BD.size()+" TotalDist: "+m_newMeanE.totalDist);
//        System.out.println(m_newMeanE.toString());
        
    }
    
    
}

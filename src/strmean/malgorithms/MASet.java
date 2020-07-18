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
public class MASet extends MAlgorithm {

    /**
     * *
     * Requires a properties object with: - EditDistance object, mapped with key
     * JConstants.EDIT_DISTANCE
     *
     * @param BD
     * @param seed
     * @param p
     * @return
     * @throws Exception
     */
    @Override
    public MAResult getMean(List<Example> BD, Example seed, Properties p) throws Exception {

        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        float betterSum = Float.MAX_VALUE;
        int betterIndex = -1;
        float tmpDist;
        int totalDist = 0;
        int dbSize = BD.size();
        Example source;

        float[] sumDist = new float[BD.size()];
        float[][] distances = new float[BD.size()][BD.size()];
        
        for (int i = 0; i < dbSize; i++) {
            source = BD.get(i);
            for (int j = i + 1; j < dbSize; j++) {
                tmpDist = ed.dEdition(source, BD.get(j), false).dist;
                totalDist++;
                sumDist[i] = sumDist[i] + tmpDist;
                sumDist[j] = sumDist[j] + tmpDist;
                
                distances[i][j]=distances[j][i]=tmpDist;
            }

            if (sumDist[i] < betterSum) {
                betterIndex = i;
                betterSum = sumDist[i];
            }
        }

        MAResult result = new MAResult();
        result.sumDist = betterSum;
        result.meanExample = BD.get(betterIndex);
        result.totalDist = totalDist;
        result.distances = distances[betterIndex];
        return result;
    }

    public static void main(String[] args) throws Exception {


        //<editor-fold defaultstate="collapsed" desc="injecting dependencies">
        Properties p = JUtils.loadProperties();
        String sdType = p.getProperty(JConstants.SYMBOL_DIF);
        SymbolDif sd = JUtils.newInstance(SymbolDif.class, sdType, p);
        p.put(JConstants.SYMBOL_DIF, sd);

        String edType = p.getProperty(JConstants.EDIT_DISTANCE);
        EditDistance eD = JUtils.newInstance(EditDistance.class, edType, p);
        p.put(JConstants.EDIT_DISTANCE, eD);
        //</editor-fold>

        List<Example> BD = JUtils.loadExamples(args[0]);
        MASet sm = new MASet();

        MAResult setMean = sm.getMean(BD, null, p);
        System.out.println("SetMedian AvgDist: " + setMean.sumDist / BD.size() + " TotalDist: " + (setMean.totalDist));
        System.out.println(setMean.meanExample.toString());

    }

}

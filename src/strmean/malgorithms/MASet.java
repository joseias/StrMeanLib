package strmean.malgorithms;

import java.util.List;
import java.util.Properties;
import strmean.data.Example;
import strmean.data.MAResult;
import strmean.distances.EditDistance;
import strmean.main.JConstants;

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

        //<editor-fold defaultstate="collapsed" desc="Injecting dependencies">
        EditDistance ed = (EditDistance) p.get(JConstants.EDIT_DISTANCE);
        //</editor-fold>

        float betterSum = Float.MAX_VALUE;
        int betterIndex = -1;
        float tmpDist;
        int totalDist = 0;
        int dbSize = BD.size();
        Example source;

        float[] sumDist = new float[BD.size()];

        for (int i = 0; i < dbSize; i++) {
            source = BD.get(i);
            for (int j = i + 1; j < dbSize; j++) {
                tmpDist = ed.dEdition(source, BD.get(j), false).dist;
                totalDist++;
                sumDist[i] = sumDist[i] + tmpDist;
                sumDist[j] = sumDist[j] + tmpDist;
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
        return result;
    }

    public static void main(String[] args) throws Exception {

//        String cmpType=JUtils.GetArgsType(JConstants.PROPERTIES_FILE, "symbolDif");
//        SymbolDif sd=(SymbolDif)JUtils.BuildInstance(cmpType);
//        
//        ArrayList<Example> BD=JUtils.LoadExamples(args[0]);
//        MASet js=new MASet(new EDLevenshtein());
//        
//        Example newMeanE=js.getMean(BD,new Example("0","",sd));
//        System.out.println("SetMean AvgDist: "+newMeanE.sumDist/BD.size()+" TotalDist: "+newMeanE.totalDist);
//        System.out.println(newMeanE.toString());
    }

}

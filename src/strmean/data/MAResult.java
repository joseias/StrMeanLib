package strmean.data;

import java.util.List;

public class MAResult {

    public Example meanExample;
    public float sumDist;
    public float[] distances;
    public int totalDist;
    public List<String> opPosList;

    public MAResult() {
        sumDist = 0;
        totalDist = 0;
    }
}

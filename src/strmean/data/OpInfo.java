package strmean.data;

public class OpInfo {

    public float cost = 0;         //cost of the operation (static)
    public int votes = 0;          // number of times the operation is reapeated...for statistics purpose
    public float quality = 0;    //stores the quality of an operation evaluated by the OpStats
    public float weigth = 1; // Similar a votes, pero usado en HSP

    public OpInfo(float cost, int votes, float quality) {
        this.cost = cost;
        this.votes = votes;
        this.quality = quality;
    }
}

package strmean.opstateval;

import java.util.List;
import strmean.data.Example;
import strmean.data.Operation;
import strmean.data.SymbolDif;

public abstract class OpStats {

    public float sumDist;
    public float[] distances;
    public int totalDist;
    public Example ex;
    public SymbolDif sDif;

    public OpStats() {
    }

    public abstract OpStats newInstance();

    public void init(Example ex, SymbolDif sDif, int dbSize) {
        this.sDif = sDif;
        this.ex = ex;

        totalDist = 0;
        sumDist = 0;

        distances = new float[dbSize];
    }

    public abstract void addOperation(Operation op);

    public abstract List<Operation> getOperations();

}

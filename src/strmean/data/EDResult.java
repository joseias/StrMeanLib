package strmean.data;

import java.util.ArrayList;
import java.util.List;

public class EDResult {

    private List<Operation> operations;

    public Example ex;
    public Example ey;

    public float dist;
    public boolean procIndirectSubstitutions;

    SymbolDif sDif;

    public EDResult(Example ex, Example ey) {
        this.ex = ex;
        this.ey = ey;

        operations = new ArrayList<>(ex.sequence.length + ex.sequence.length + 1);
    }

    public List<Operation> getOperations() {
        return this.operations;
    }

}

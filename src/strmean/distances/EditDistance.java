package strmean.distances;

import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.SymbolDif;
import strmean.main.JConstants;

public abstract class EditDistance {

    public SymbolDif _sd;

    EditDistance(Properties p) {
        this._sd = (SymbolDif) p.get(JConstants.SYMBOL_DIF);
    }

    public abstract EDResult dEdition(Example ex, Example ey, boolean computeStatistics);
}

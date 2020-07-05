package strmean.opstateval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import strmean.data.Example;
import strmean.data.Operation;
import strmean.data.SymbolDif;

/**
 * Similar to OpStatsJR but quality = votes * cost, instead quality = votes
 *
 */
public class OpStatsJRF extends OpStats {

    public HashMap<Operation, Integer>[] I;
    public HashMap<Operation, Integer>[] D;
    public HashMap<Operation, Integer>[] S;
    public HashMap<Operation, Integer>[] W;

    public OpStatsJRF() {
        super();
    }

    @Override
    public void init(Example ex, SymbolDif sDif, int dbSize) {
        super.init(ex, sDif, dbSize);

        I = new HashMap[ex.sequence.length + 1];
        for (int i = 0; i < I.length; i++) {
            I[i] = new HashMap<>();
        }

        D = new HashMap[ex.sequence.length + 1];
        for (int d = 0; d < D.length; d++) {
            D[d] = new HashMap<>();
        }

        S = new HashMap[ex.sequence.length + 1];
        for (int s = 0; s < S.length; s++) {
            S[s] = new HashMap<>();
        }

        W = new HashMap[ex.sequence.length + 1];
        for (int w = 0; w < W.length; w++) {
            W[w] = new HashMap<>();
        }
    }

    @Override
    public void addOperation(Operation op) {
        int pos = op.posSource;
        int tmpOpCount;
        char opType = op.type;

        switch (opType) {
            case 'i':
                tmpOpCount = I[pos].containsKey(op) ? I[pos].get(op) + 1 : 1;
                I[pos].put(op, tmpOpCount);
                break;

            case 'd':
                tmpOpCount = D[pos].containsKey(op) ? D[pos].get(op) + 1 : 1;
                I[pos].put(op, tmpOpCount);
                break;

            case 's':
                tmpOpCount = S[pos].containsKey(op) ? S[pos].get(op) + 1 : 1;
                S[pos].put(op, tmpOpCount);
                break;

            case 'w':
                tmpOpCount = W[pos].containsKey(op) ? W[pos].get(op) + 1 : 1;
                W[pos].put(op, tmpOpCount);
                break;

            default:
                System.err.println("Invalid operation in Example.SetOperation...");
        }
    }

    @Override
    public List<Operation> getOperations() {
        int totalLength = I.length + D.length + S.length + W.length;

        ArrayList<Operation> result = new ArrayList<>(totalLength);
        for (HashMap<Operation, Integer> I1 : this.I) {
            for (Operation op : I1.keySet()) {
                op.opInfo.votes = I1.get(op);
                op.opInfo.quality = op.opInfo.votes * op.opInfo.cost;
                result.add(op);
            }
        }
        for (HashMap<Operation, Integer> D1 : this.D) {
            for (Operation op : D1.keySet()) {
                op.opInfo.votes = D1.get(op);
                op.opInfo.quality = op.opInfo.votes * op.opInfo.cost;
                result.add(op);
            }
        }

        for (HashMap<Operation, Integer> S1 : this.S) {
            for (Operation op : S1.keySet()) {
                op.opInfo.votes = S1.get(op);
                op.opInfo.quality = op.opInfo.votes * op.opInfo.cost;
                result.add(op);
            }
        }

        for (HashMap<Operation, Integer> W1 : this.W) {
            for (Operation op : W1.keySet()) {
                op.opInfo.votes = W1.get(op);
                op.opInfo.quality = op.opInfo.votes * op.opInfo.cost;
                result.add(op);
            }
        }

        return result;
    }

    @Override
    public OpStats newInstance() {
        return new OpStatsJRF();
    }
}

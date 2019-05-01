package strmean.opstateval;

import java.util.Arrays;
import java.util.Comparator;
import strmean.data.OpInfo;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.stream.Collectors;
import strmean.main.JUtils;
import strmean.data.SymbolDif;
import strmean.data.Example;
import strmean.data.Operation;
import strmean.main.JConstants;

/**
 * MIRABAL, P., J. ABREU, y D. SECO, . "Assessing the best edit in
 * perturbation-based iterative refinement algorithms to compute the median
 * string". Pattern Recognition Letters. 2019, vol 120, p. 104 - 111.
 *
 * @author sijfg
 */
public class OpStatsP extends OpStats {

    Opp[] positions;
    Opp[] positionsAfter;
    int alfabetSize;
    char[] source;

    int maxOpsToInclude;

    /* Max number of operations to consider...*/

    /**
     *
     * @param ex
     * @param sDif
     * @param dbSize
     */
    @Override
    public void init(Example ex, SymbolDif sDif, int dbSize) {
        super.init(ex, sDif, dbSize);

        Properties p = JUtils.loadProperties();
        maxOpsToInclude = Integer.parseInt(p.getProperty(JConstants.MAX_OPS, "1"));

        /*JComment:  MAX_OPS=0 to indicate an unbounded number of operations...
          This patch must be change since by default maxOpsToInclude = Inf
         */
        maxOpsToInclude = Math.max(maxOpsToInclude, 1);

        source = ex.sequence;
        alfabetSize = sDif.c_AlphabetSize;
        positions = new Opp[source.length];
        positionsAfter = new Opp[source.length];

        Arrays.setAll(positions, i -> {
            Opp op = new Opp();
            op.insertions = new float[alfabetSize];
            op.sustitutions = new float[alfabetSize];
            return op;
        });

        Arrays.setAll(positionsAfter, i -> {
            Opp op = new Opp();
            op.insertions = new float[alfabetSize];
            op.sustitutions = new float[alfabetSize];
            return op;
        });
    }

    @Override
    public void addOperation(Operation a_op) {
        int symbolIndexB = sDif.getIndex(a_op.b);
        int pos = a_op.posSource;

        switch (a_op.type) {
            case 'i':
                positions[pos].insertions[symbolIndexB] += a_op.opInfo.weigth;
                break;

            case 'd':
                positions[pos].deletes += a_op.opInfo.weigth;
                break;

            case 's':
                positions[pos].sustitutions[symbolIndexB] += a_op.opInfo.weigth;
                break;

            case 'w':
                /*For Damerau-Levenshtein*/
                break;

            default:
                System.err.println("Invalid operation in Example.SetOperation...");
        }
    }

    @Override
    public List<Operation> getOperations() {

        for (int pos = 0; pos < this.positionsAfter.length; ++pos) {
            char symbolI;
            char symbolJ;

            for (int i = 0; i < this.alfabetSize; ++i) {

                symbolI = this.sDif.getSymbol(i);
                for (int j = 0; j < this.alfabetSize; ++j) {
                    if (j != i) {
                        symbolJ = this.sDif.getSymbol(j);
                        float sus = this.sDif.sus(symbolI, symbolJ);

                        this.positionsAfter[pos].insertions[i] += (this.sDif.ins(symbolJ) - sus) * this.positions[pos].insertions[j];
                        this.positionsAfter[pos].sustitutions[i] += (this.sDif.sus(this.source[pos], symbolJ) - sus) * this.positions[pos].sustitutions[j];
                    }
                }
            }
        }

        /* Preprocessing... */
        Comparator<Operation> opCmp = (Operation o1, Operation o2) -> Float.compare(o1.opInfo.quality, o2.opInfo.quality);
        PriorityQueue<Operation> ops = new PriorityQueue<>(opCmp);

        float tmpQuality;
        for (int pos = 0; pos < this.positionsAfter.length; ++pos) {

            /*Insertions*/
            for (int i = 0; i < this.positionsAfter[pos].insertions.length; ++i) {
                tmpQuality = this.positions[pos].insertions[i] * this.sDif.ins(this.sDif.getSymbol(i)) + this.positionsAfter[pos].insertions[i];
                this.addOpIfPossibleSorI(ops, 'i', pos, i, tmpQuality);
            }

            /*Sustitutions*/
            for (int i = 0; i < this.positionsAfter[pos].sustitutions.length; ++i) {
                tmpQuality = this.positions[pos].sustitutions[i] * this.sDif.sus(this.source[pos], this.sDif.getSymbol(i)) + this.positionsAfter[pos].sustitutions[i];
                this.addOpIfPossibleSorI(ops, 's', pos, i, tmpQuality);
            }

            /*Deletions*/
            tmpQuality = this.positions[pos].deletes * this.sDif.del(this.source[pos]);
            this.addOpIfPossibleD(ops, 'd', pos, tmpQuality);
        }

        return ops.stream().collect(Collectors.toList());
    }

    @Override
    public OpStats newInstance() {
        return new OpStatsP();
    }

    private void addOpIfPossibleSorI(PriorityQueue<Operation> result, char opType, int pos, int i, float tmpQuality) {
        if (result.size() < this.maxOpsToInclude) {
            result.offer(new Operation(opType,
                    this.source[pos],
                    this.sDif.getSymbol(i),
                    pos,
                    0,
                    new OpInfo(0.0f, 0, tmpQuality)));
        }
        else {
            if (result.peek().opInfo.quality < tmpQuality) {
                result.poll();
                result.add(new Operation('i',
                        this.source[pos],
                        this.sDif.getSymbol(i),
                        pos,
                        0,
                        new OpInfo(0.0f, 0, tmpQuality)));
            }
        }
    }

    private void addOpIfPossibleD(PriorityQueue<Operation> result, char opType, int pos, float tmpQuality) {
        if (result.size() < this.maxOpsToInclude) {
            result.add(new Operation(opType,
                    this.source[pos],
                    this.source[pos],
                    pos,
                    0,
                    new OpInfo(0.0f, 0, tmpQuality)));

        }
        else {
            if (result.peek().opInfo.quality < tmpQuality) {
                result.poll();
                result.add(new Operation(opType,
                        this.source[pos],
                        this.source[pos],
                        pos,
                        0,
                        new OpInfo(0.0f, 0, tmpQuality)));
            }
        }
    }

    private class Opp {

        public float[] insertions;

        public float[] sustitutions;

        public float deletes;

    }
}

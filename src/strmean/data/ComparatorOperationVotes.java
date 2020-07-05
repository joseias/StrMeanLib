package strmean.data;

import java.util.Comparator;

public class ComparatorOperationVotes implements Comparator<Operation> {
    // overriding the compare method

    @Override
    public int compare(Operation a, Operation b) {
        return (int) Math.signum(b.opInfo.votes - a.opInfo.votes);
    }
}

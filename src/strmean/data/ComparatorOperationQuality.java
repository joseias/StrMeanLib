package strmean.data;

import java.util.Comparator;

public class ComparatorOperationQuality implements Comparator<Operation> {

    @Override
    public int compare(Operation a, Operation b) {
        return (int) Math.signum(b.opInfo.quality - a.opInfo.quality);
    }
}

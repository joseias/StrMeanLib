package strmean.data;

import java.util.Comparator;

/**
 *
 * @author juanra
 */
public class ComparatorOperationQuality implements Comparator<Operation> {
    // overriding the compare method

    @Override
    public int compare(Operation a, Operation b) 
    {
        return (int) Math.signum(b.opInfo.quality - a.opInfo.quality);
    }
}

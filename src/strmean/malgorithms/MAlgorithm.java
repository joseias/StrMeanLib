package strmean.malgorithms;

import java.util.List;
import java.util.Properties;
import strmean.data.Example;
import strmean.data.MAResult;

/**
 * @author jabreu
 */
public abstract class MAlgorithm {

    public abstract MAResult getMean(List<Example> iset, Example seed, Properties p) throws Exception;
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.malgorithms;

import java.util.List;
import java.util.Properties;
import strmean.data.Example;
import strmean.data.MAResult;

/**
 *
 * @author jabreu
 */
public abstract class MAlgorithm
{    
    public abstract MAResult getMean(List<Example> a_BD, Example a_seed,Properties p) throws Exception;
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.data;

import java.util.List;

/**
 *
 * @author sijfg
 */
public class MAResult {
    public Example meanExample;
    public float sumDist;
    public float[] distances;
    public int totalDist;
    public List<String> opPosList;
    
    public MAResult(){
        sumDist=0;
        totalDist=0;
    }
}

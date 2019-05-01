/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.opstateval;

import java.util.List;
import strmean.data.Example;
import strmean.data.Operation;
import strmean.data.SymbolDif;

/**
 *
 * @author Docente
 */
public abstract class OpStats {
    public float sumDist;
    public float[] distances;
    public int totalDist;
    public Example ex;
    public SymbolDif sDif;
    
    public OpStats(){}
    
    public abstract OpStats newInstance();
    public  void init(Example ex,SymbolDif sDif,int dbSize){    
        this.sDif=sDif;
        this.ex=ex;

        totalDist=0;
        sumDist=0;

        distances=new float[dbSize];
    }
    
    public abstract void addOperation(Operation a_op);
    public abstract List<Operation> getOperations();
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Toshiba PC
 */
public class EDResult {
    
    


    private List<Operation> operations;
    
    public Example ex;
    public Example ey;
    
    public float dist;
    public boolean procIndirectSubstitutions;
    
    SymbolDif sDif;
    
    public EDResult(Example ex,Example ey){
        this.ex=ex;
        this.ey=ey;
        
        operations=new ArrayList<>(ex.sequence.length+ex.sequence.length+1);
    }

    public List<Operation> getOperations(){
        return this.operations;
    }
    
}

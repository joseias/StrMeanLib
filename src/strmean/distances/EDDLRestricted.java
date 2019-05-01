package strmean.distances;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.OpInfo;
import strmean.data.Operation;

/*https://github.com/KevinStern/software-and-algorithms/blob/master/src/main/java/blogspot/software_and_algorithms/stern_library/string/DamerauLevenshteinAlgorithm.java*/
public class EDDLRestricted extends EditDistance{
  private float deleteCost=0;
  private float insertCost=0;
  private float replaceCost=0;
  private float swapCost=0;

    public EDDLRestricted(Properties p) {
        super(p);
        
        try{
            deleteCost=Float.parseFloat(p.get("DL_DEL").toString());
            insertCost=Float.parseFloat(p.get("DL_INS").toString());
            replaceCost=Float.parseFloat(p.get("DL_REP").toString());
            swapCost=Float.parseFloat(p.get("DL_SWAP").toString());
            
        }
        catch(NumberFormatException m_e){
            m_e.printStackTrace();
        }  
    }

    /**
     *
     * @param ex
     * @param ey
     * @param computeStatistics
     * @return
     */
    @Override
  public EDResult dEdition(Example ex, Example ey, boolean computeStatistics) {
    
    EDResult result=new EDResult(ex, ey);
    
    String source=new String(ex.sequence);
    String target=new String(ey.sequence);
    int sourceL=source.length()+1;
    int targetL=target.length()+1;
    
    float[][] table = new float[sourceL][targetL];
    String[][] tableOps = new String[sourceL][targetL];
    tableOps[0][0]="-";
        
    Map<Character, Integer> sourceIndexByCharacter = new HashMap<>();

    if(source.length()==0){
        sourceIndexByCharacter.put(' ',0);
    }
    else{
        sourceIndexByCharacter.put(source.charAt(0),0);
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="Calculo de la distancia">
    for (int i = 1; i < sourceL; i++)
    {
        tableOps[i][0]="d";
        table[i][0]=deleteCost*i;
    }
    
    for (int j = 1; j < targetL; j++)
    {
        tableOps[0][j]="i";
        table[0][j]=insertCost*j;
    }
    
    for (int i = 1; i < sourceL; i++)
    {
      int maxSourceLetterMatchIndex;
      if(target.length()==0){
          maxSourceLetterMatchIndex=0;
      }
      else{
          maxSourceLetterMatchIndex = source.charAt(i-1) == target.charAt(0) ? 0: -1;
      }
      
      for (int j = 1; j < targetL; j++)
      {
        Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j-1));
        
        int jSwap = maxSourceLetterMatchIndex;
        float deleteDistance = table[i - 1][j] + deleteCost;
        float insertDistance = table[i][j - 1] + insertCost;
        float matchDistance = table[i - 1][j - 1];
        
        if (source.charAt(i-1) != target.charAt(j-1))
        {
          matchDistance += replaceCost;
        } 
        else 
        {
          maxSourceLetterMatchIndex = j;
        }
        
        float swapDistance;
        if (candidateSwapIndex != null && jSwap != -1) {
          int iSwap = candidateSwapIndex;
          float preSwapCost;
          if (iSwap == 0 && jSwap == 0)
          {
            preSwapCost = 0;
          } 
          else 
          {
            preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
          }
          swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost + swapCost;
        } 
        else 
        {
          swapDistance = Integer.MAX_VALUE;
        }
        
        table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
        
        if (deleteDistance <= insertDistance &&  deleteDistance <= matchDistance &&  deleteDistance <= swapDistance) 
        {
            tableOps[i][j] = "d";
        }
        else 
        {
          if (insertDistance <= deleteDistance &&  insertDistance <= matchDistance &&  insertDistance <= swapDistance)
          {
              tableOps[i][j] = "i";
          }
          else
          {
              if (swapDistance <= deleteDistance &&  swapDistance <= matchDistance &&  swapDistance <= insertDistance) 
              {
                tableOps[i][j] = "w";
              }
              else 
              {
                  tableOps[i][j] = "s";
              }
          }
        } 
      }
      sourceIndexByCharacter.put(source.charAt(i-1), i);
    }
     
  //</editor-fold>
    
    
//  Imprimir operaciones
//     for (int i = 0; i < sourceL; i++) {
//          for (int j = 0; j < targetL; j++) {
//              System.out.print(tableOps[i][j]);
//          }
//        System.out.print("\n");  
//      }
//      for (int i = 0; i < sourceL i++) {
//          for (int j = 0; j < targetL; j++) {
//              System.out.print(table[i][j]);
//          }
//        System.out.println("\n");  
//      }
      
      /*============ Buscar operaciones ============*/
      //esto lo tengo que mirar
      int mi = sourceL-1;
      int mj = targetL-1;
      int seqOrd=0; //no sé pa que se usa
      
      int lastOppPos = sourceL -2;
      char prevOp=' ';
      
      while(mi > 0 || mj > 0 ){        
        switch (tableOps[mi][mj].charAt(0)) {
            case 'w':
                {
                    prevOp='w';
                    Operation o=new Operation(
                            tableOps[mi][mj].charAt(0),
                            source.charAt(mi-2),
                            source.charAt(mi-1),
                            lastOppPos -1,
                            seqOrd,
                            new OpInfo(this.swapCost,0,0));      
                    
                    result.getOperations().add(0,o);
                    seqOrd++;
                    lastOppPos -= 2;
                    mj -= 2;
                    mi -= 2;
                    break;
                }
            case 'd':
                {
                    prevOp='d';
                    Operation o=new Operation(
                            tableOps[mi][mj].charAt(0),
                            source.charAt(mi-1),
                            source.charAt(mi-1),
                            lastOppPos,
                            seqOrd,
                            new OpInfo(this.deleteCost,0,0)); 
                    
                    result.getOperations().add(0,o);
                    seqOrd++;
                    mi -= 1;
                    lastOppPos -= 1;
                    break;
                }
            case 'i':
                {
                    prevOp='i';
                    Operation o=new Operation(
                            tableOps[mi][mj].charAt(0),
                            target.charAt(mj-1),
                            target.charAt(mj-1),
                            lastOppPos,
                            seqOrd,
                            new OpInfo(this.insertCost,0,0)); 
                    
                    result.getOperations().add(0,o);
                    seqOrd++;
                    mj -= 1;
                    break;
                }
            default:
                {
                    prevOp='s';
                    Operation o=new Operation(tableOps[mi][mj].charAt(0),
                            source.charAt(mi-1),
                            target.charAt(mj-1),
                            lastOppPos,
                            seqOrd,
                            new OpInfo(this.replaceCost,0,0));      
                    
                    result.getOperations().add(0,o);
                    seqOrd++;
                    lastOppPos--;
                    mi -= 1;
                    mj -= 1;
                    break;
                }
        }
      }//while
      
//        if (prevOp != 'w' && prevOp != 's') {
//            if (tableOps[0][0].charAt(0) == 'd') {
//            Operation o=new Operation(
//                tableOps[0][0].charAt(0), 
//                source.charAt(0),
//                source.charAt(0), 
//                lastOppPos,
//                seqOrd,
//                0,
//                this.deleteCost
//            );   
//            result.getOperations().add(0,o);
//            }
//            else if (tableOps[0][0].charAt(0)=='i') {
//            Operation o=new Operation(
//                tableOps[0][0].charAt(0), 
//                target.charAt(0),
//                target.charAt(0), 
//                lastOppPos,
//                seqOrd,
//                0,
//                this.insertCost
//            );
//            result.getOperations().add(0,o);
//            }
//            else{
//            Operation o=new Operation(tableOps[0][0].charAt(0), 
//                source.charAt(0),
//                target.charAt(0), 
//                lastOppPos - 1,
//                seqOrd,
//                0,
//                this.replaceCost
//            );
//            result.getOperations().add(0,o);
//            }
//        }
        
    result.dist=table[sourceL-1][targetL-1];
    return result;
  }

}

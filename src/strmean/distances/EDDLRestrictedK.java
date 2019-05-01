package strmean.distances;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import strmean.data.EDResult;
import strmean.data.Example;
import strmean.data.OpInfo;
import strmean.data.Operation;

/*https://github.com/KevinStern/software-and-algorithms/blob/master/src/main/java/blogspot/software_and_algorithms/stern_library/string/DamerauLevenshteinAlgorithm.java*/
public class EDDLRestrictedK extends EditDistance{
  private float deleteCost=0;
  private float insertCost=0;
  private float replaceCost=0;
  private float swapCost=0;

    public EDDLRestrictedK(Properties p) {
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
    
    /* Esto después tengo que arreglarlo, por ahora no llamar con cadenas vacias  
    if (source.length() == 0) {
      return target.length() * insertCost;
    }
    if (target.length() == 0) {
      return source.length() * deleteCost;
    }
    */
    float[][] table = new float[source.length()][target.length()];
    String[][] tableOps = new String[source.length()][target.length()];
    Map<Character, Integer> sourceIndexByCharacter = new HashMap<>();
    
    if (source.charAt(0) != target.charAt(0))
    {
        table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
        if (replaceCost <= deleteCost + insertCost) 
        {
            tableOps[0][0] = "s";
        }
        else
        {
            tableOps[0][0] = "di";
        }
    }
    sourceIndexByCharacter.put(source.charAt(0), 0);
    
    for (int i = 1; i < source.length(); i++)
    {
      float deleteDistance = table[i - 1][0] + deleteCost;
      float insertDistance = (i + 1) * deleteCost + insertCost;
      float matchDistance = i * deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
      
      table[i][0] = Math.min(Math.min(deleteDistance, insertDistance),matchDistance);
      
      if (deleteDistance <= insertDistance &&  deleteDistance <= matchDistance) 
      {
        tableOps[i][0] = "d";
      }
      else 
      {
          if (insertDistance <= deleteDistance &&  insertDistance <= matchDistance)
          {
              tableOps[i][0] = "i";
          }
          else
          {
              tableOps[i][0] = "s";
          }
      }
    }
    
    for (int j = 1; j < target.length(); j++)
    {
      float deleteDistance = (j + 1) * insertCost + deleteCost;
      float insertDistance = table[0][j - 1] + insertCost;
      float matchDistance = j * insertCost + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
      table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
      
      if (deleteDistance <= insertDistance &&  deleteDistance <= matchDistance)
      {
        tableOps[0][j] = "d";
      }
      else 
      {
          if (insertDistance <= deleteDistance &&  insertDistance <= matchDistance)
          {
              tableOps[0][j] = "i";
          }
          else
          {
              tableOps[0][j] = "s";
          }
      }
    }
    
    for (int i = 1; i < source.length(); i++)
    {
      int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0: -1;
      
      for (int j = 1; j < target.length(); j++)
      {
        Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j));
        
        int jSwap = maxSourceLetterMatchIndex;
        float deleteDistance = table[i - 1][j] + deleteCost;
        float insertDistance = table[i][j - 1] + insertCost;
        float matchDistance = table[i - 1][j - 1];
        
        if (source.charAt(i) != target.charAt(j))
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
      sourceIndexByCharacter.put(source.charAt(i), i);
    }
     
//       Imprimir operaciones
     for (int i = 0; i < source.length(); i++) {
          for (int j = 0; j < target.length(); j++) {
              System.out.print(tableOps[i][j]);
          }
        System.out.print("\n");  
      }
//      for (int i = 0; i < source.length(); i++) {
//          for (int j = 0; j < target.length(); j++) {
//              System.out.print(table[i][j]);
//          }
//        System.out.println("\n");  
//      }
      
      /*============ Buscar operaciones ============*/
      //esto lo tengo que mirar
      int mi = source.length()-1;
      int mj = target.length()-1;
      int seqOrd=0; //no sé pa que se usa
      int lastOppPos = source.length() -1;
      char prevOp=' ';
      
      while(mi > 0 || mj > 0 ){        
        if (tableOps[mi][mj].charAt(0) == 'w') {    
            prevOp='w';
            Operation o=new Operation(
                tableOps[mi][mj].charAt(0), 
                source.charAt(mi-1),
                source.charAt(mi), 
                lastOppPos -1,
                seqOrd,
                new OpInfo(this.swapCost,0,0));
            
            result.getOperations().add(0,o);
            seqOrd++;
            lastOppPos -= 2;
            mj -= 2;
            mi -= 2;
        }
        else if (tableOps[mi][mj].charAt(0) == 'd') {
            prevOp='d';
            Operation o=new Operation(
                tableOps[mi][mj].charAt(0), 
                source.charAt(mi),
                source.charAt(mi), 
                lastOppPos,
                seqOrd,
                new OpInfo(this.deleteCost,0,0));
            
            result.getOperations().add(0,o);
            seqOrd++;
            mi -= 1;
            lastOppPos -= 1;
        }
        else if (tableOps[mi][mj].charAt(0)=='i') {
            prevOp='i';
            Operation o=new Operation(
                tableOps[mi][mj].charAt(0), 
                target.charAt(mj),
                target.charAt(mj), 
                lastOppPos,
                seqOrd,
                new OpInfo(this.insertCost,0,0));
            
            result.getOperations().add(0,o);
            seqOrd++;
            mj -= 1;
        }
        else{
            prevOp='s';
            Operation o=new Operation(tableOps[mi][mj].charAt(0), 
                source.charAt(mi),
                target.charAt(mj), 
                lastOppPos,
                seqOrd,
                new OpInfo(this.replaceCost,0,0));
            
            result.getOperations().add(0,o);
            seqOrd++;
            lastOppPos--;
            mi -= 1;
            mj -= 1;
        }
      }
        if (prevOp != 'w' && prevOp != 's') {
            if (tableOps[0][0].charAt(0) == 'd') {
            Operation o=new Operation(
                tableOps[0][0].charAt(0), 
                source.charAt(0),
                source.charAt(0), 
                lastOppPos,
                seqOrd,
                new OpInfo(this.deleteCost,0,0));
            
            result.getOperations().add(0,o);
            }
            else if (tableOps[0][0].charAt(0)=='i') {
            Operation o=new Operation(
                tableOps[0][0].charAt(0), 
                target.charAt(0),
                target.charAt(0), 
                lastOppPos,
                seqOrd,
                new OpInfo(this.insertCost,0,0));
            
            result.getOperations().add(0,o);
            }
            else{
            Operation o=new Operation(tableOps[0][0].charAt(0), 
                source.charAt(0),
                target.charAt(0), 
                lastOppPos - 1,
                seqOrd,
                new OpInfo(this.replaceCost,0,0));
            
            result.getOperations().add(0,o);
            }
        }
        
    result.dist=table[source.length()-1][target.length()-1];
    return result;
  }
}

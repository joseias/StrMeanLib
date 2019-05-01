package strmean.opstateval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import strmean.data.Example;
import strmean.data.Operation;
import strmean.data.SymbolDif;

/**ABREU, J. y J.R. RICO-JUAN, . "A new iterative algorithm for computing a quality approximate median of strings based on edit operations". 
 * Pattern Recognition Letters. 2014, vol 36, núm. 0, p. 74 - 80.
 * @author sijfg
 */
public class OpStatsJR extends OpStats{
        public HashMap<Operation,Integer>[] I;
        public HashMap<Operation,Integer>[] D;
        public HashMap<Operation,Integer>[] S;
        public HashMap<Operation,Integer>[] W;
         
        public OpStatsJR(){
            super();
        }
        
        @Override
        public void init(Example ex,SymbolDif sDif,int dbSize){      
            super.init(ex, sDif, dbSize);
            
            I=new HashMap[ex.sequence.length+1];
            for(int i=0;i<I.length;i++){I[i]=new HashMap<>();}

            D= new HashMap[ex.sequence.length+1];
            for(int d=0;d<D.length;d++){D[d]=new HashMap<>();}

            S=new HashMap[ex.sequence.length+1];
            for(int s=0;s<S.length;s++){S[s]=new HashMap<>();}

            W=new HashMap[ex.sequence.length+1];
            for(int w=0;w<W.length;w++){W[w]=new HashMap<>();}
        }
        
        @Override
        public void addOperation(Operation a_op)
        {
            int m_pos=a_op.posSource;
            int m_tmpOpCount;
            char m_opType=a_op.type;

            switch(m_opType)
            {
                case 'i':
                    m_tmpOpCount=I[m_pos].containsKey(a_op)?I[m_pos].get(a_op)+1 :1;
                    I[m_pos].put(a_op,m_tmpOpCount);
                    break;

                case 'd':
                    m_tmpOpCount=D[m_pos].containsKey(a_op)?D[m_pos].get(a_op)+1 :1;
                    I[m_pos].put(a_op,m_tmpOpCount);
                    break;

                case 's':
                    m_tmpOpCount=S[m_pos].containsKey(a_op)?S[m_pos].get(a_op)+1 :1;
                    S[m_pos].put(a_op,m_tmpOpCount);
                    break;

                case 'w':
                    m_tmpOpCount=W[m_pos].containsKey(a_op)?W[m_pos].get(a_op)+1 :1;
                    W[m_pos].put(a_op,m_tmpOpCount);
                    break;

                default:
                    System.err.println("Invalid operation in Example.SetOperation...");
            }
        }
        
        @Override
        public List<Operation> getOperations(){
            int totalLength=I.length+D.length+S.length+W.length;
            
            ArrayList<Operation> m_result=new ArrayList<>(totalLength);
            for (HashMap<Operation, Integer> I1 : this.I) {
                for (Operation op : I1.keySet()) {
                    op.opInfo.votes = I1.get(op);
                    op.opInfo.quality=op.opInfo.votes;
                    m_result.add(op);
                }
            }
            for (HashMap<Operation, Integer> D1 : this.D) {
                for (Operation op : D1.keySet()) {
                    op.opInfo.votes = D1.get(op);
                    op.opInfo.quality=op.opInfo.votes;
                    m_result.add(op);
                }
            }

            for (HashMap<Operation, Integer> S1 : this.S) {
                for (Operation op : S1.keySet()) {
                    op.opInfo.votes = S1.get(op);
                    op.opInfo.quality=op.opInfo.votes;
                    m_result.add(op);
                }
            }
            
            for (HashMap<Operation, Integer> W1 : this.W) {
                for (Operation op : W1.keySet()) {
                    op.opInfo.votes = W1.get(op);
                    op.opInfo.quality=op.opInfo.votes;
                    m_result.add(op);
                }
            }
                        
            return m_result;
        }
        
        @Override
        public OpStats newInstance() {
           return new OpStatsJR();
        }       
    }
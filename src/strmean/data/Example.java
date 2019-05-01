package strmean.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import strmean.main.JConstants;
import strmean.main.JUtils;

/*
 * Example to ChainsSetMulti
 */
public class Example {

    public String category;
    public String ID;
    public char[] sequence = null;
    private int hashCode;
    
//    public float sumDist = 0f; //Sum distances to rest of examples
//    public long totalDist=0;
    
    public float c_RS1=0; 
    public float c_RS2=0; 
    public float weigth=1;
    
    public Example(String cl, String co)
    {
        category = cl;
        sequence = co.toCharArray();
        hashCode=co.hashCode();    
    }

    public Example(Example e) {
        this.category=e.category;
        this.ID=e.ID;
        this.sequence=e.sequence;
//        this.sumDist=e.sumDist;
//        this.totalDist=e.totalDist;
        this.c_RS1=e.c_RS1;
        this.c_RS2=e.c_RS2;
        this.weigth=e.weigth;
    }

    /**
     *
     * @param a_ops
     * @return
     */
    public Example applyOperations(List<Operation> a_ops) 
    {
        //<editor-fold defaultstate="collapsed" desc="Ordernar de acuerdo a la posicion">
           /*Se asume ordenada*/
        Collections.sort(a_ops,new ComparatorOperationPos());
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Aplicar operaciones">
        int m_lastPos=0;
        String m_newContour="";
        for(Operation op:a_ops)
        { 
            //<editor-fold defaultstate="collapsed" desc="Avanzar las posiciones no involucradas en una operacion">
            while(m_lastPos<op.posSource)
            {
                m_newContour=m_newContour.concat(Character.toString(sequence[m_lastPos]));
                m_lastPos++;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Analizar la operacion en cuestion">
            switch(op.type)
            {
                /*Insertion*/
                case 'i':
                    m_newContour=m_newContour.concat(Character.toString(op.a));
                    break;
                    
                /*Deletion*/    
                case 'd':
                    m_lastPos++;
                    break;
                    
                /*Substitution*/    
                case 's':
                    m_newContour=m_newContour.concat(Character.toString(op.b));
                    m_lastPos++;
                    break;
                    
                /*Transposition*/
                case 'w':
                    m_newContour=m_newContour.concat(Character.toString(op.b));
                    m_lastPos++;
                    
                    m_newContour=m_newContour.concat(Character.toString(op.a));
                    m_lastPos++;
                    break;
                    
                default:
                    System.err.println("Invalid operation in Example.ApplyOperations...");
            }
                        
            //</editor-fold>
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Terminar de procesar la cadena a partir de la posicion de la ultima operacion">
        while(m_lastPos<this.sequence.length)
        {
            m_newContour=m_newContour.concat(Character.toString(sequence[m_lastPos]));
            m_lastPos++;
        }
 
        //</editor-fold>
        return new Example(this.category,m_newContour);
    }
    
    @Override
    public Example clone() throws CloneNotSupportedException
    {
        Example m_result=new Example(this);
        return m_result;
    }

    @Override
    public String toString() {
        return category + " " + (new String(sequence));
    }
    
    @Override
    public int hashCode()
    {
        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Example other = (Example) obj;
        if (this.hashCode != other.hashCode) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception
    {
        String m_cmpType=JUtils.getArgsType(JConstants.PROPERTIES_FILE, "symbolDif");
        SymbolDif a_sd=(SymbolDif)JUtils.buildInstanceFromType(m_cmpType);

        Example e=new Example("A","01234567");
        Operation o1=new Operation('w', '1', '2',2,0,new OpInfo(0, 0,0));

        ArrayList<Operation> ops=new ArrayList<>(1);
        

        ops.add(o1);
        
        Example m_tmp=e.applyOperations(ops);
        System.out.println(m_tmp.toString());
        
        
    }
    
}

package strmean.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public float c_RS1 = 0;
    public float c_RS2 = 0;
    public float weigth = 1;

    public Example(String cl, String co) {
        category = cl;
        sequence = co.toCharArray();
        hashCode = co.hashCode();
    }

    public Example(Example e) {
        this.category = e.category;
        this.ID = e.ID;
        this.sequence = e.sequence;
//        this.sumDist=e.sumDist;
//        this.totalDist=e.totalDist;
        this.c_RS1 = e.c_RS1;
        this.c_RS2 = e.c_RS2;
        this.weigth = e.weigth;
    }

    /**
     *
     * @param ops
     * @return
     */
    public Example applyOperations(List<Operation> ops) {
        //<editor-fold defaultstate="collapsed" desc="Ordernar de acuerdo a la posicion">
        /*Se asume ordenada*/
        Collections.sort(ops, new ComparatorOperationPos());
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Aplicar operaciones">
        int lastPos = 0;
        String newContour = "";
        for (Operation op : ops) {
            //<editor-fold defaultstate="collapsed" desc="Avanzar las posiciones no involucradas en una operacion">
            while (lastPos < op.posSource) {
                newContour = newContour.concat(Character.toString(sequence[lastPos]));
                lastPos++;
            }
            //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Analizar la operacion en cuestion">
            switch (op.type) {
                /*Insertion*/
                case 'i':
                    newContour = newContour.concat(Character.toString(op.a));
                    break;

                /*Deletion*/
                case 'd':
                    lastPos++;
                    break;

                /*Substitution*/
                case 's':
                    newContour = newContour.concat(Character.toString(op.b));
                    lastPos++;
                    break;

                /*Transposition*/
                case 'w':
                    newContour = newContour.concat(Character.toString(op.b));
                    lastPos++;

                    newContour = newContour.concat(Character.toString(op.a));
                    lastPos++;
                    break;

                default:
                    System.err.println("Invalid operation in Example.ApplyOperations...");
            }

            //</editor-fold>
        }
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Terminar de procesar la cadena a partir de la posicion de la ultima operacion">
        while (lastPos < this.sequence.length) {
            newContour = newContour.concat(Character.toString(sequence[lastPos]));
            lastPos++;
        }

        //</editor-fold>
        return new Example(this.category, newContour);
    }

    @Override
    public Example clone() throws CloneNotSupportedException {
        Example result = new Example(this);
        return result;
    }

    @Override
    public String toString() {
        return category + " " + (new String(sequence));
    }

    @Override
    public int hashCode() {
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

    public static void main(String[] args) throws Exception {

        Example e = new Example("A", "01234567");
        Operation o1 = new Operation('w', '1', '2', 2, 0, new OpInfo(0, 0, 0));

        ArrayList<Operation> ops = new ArrayList<>(1);

        ops.add(o1);

        Example tmp = e.applyOperations(ops);
        System.out.println(tmp.toString());

    }

}

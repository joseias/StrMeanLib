package strmean.main;

import java.util.HashMap;

public final class JStats {

    public int c_distOps;  //Distancias calculadas para sacar estadisticas de operaciones frecuentes.
    public int c_distVer;  //Distancias al resto de los elementos para ver si mejora la solucion actual
    public int c_totalDist = 0;  //Numero total de distancias calculadas.
    public HashMap<Integer, Integer> c_opIndex; // Cuenta las veces que mejoro la i-esima operación
    public int c_opI;
    public int c_innecesaryChanges;
    public int c_missedChanges;
    public int c_discardedChanges;
    public int c_successfulChanges;

    public JStats() {
        c_opIndex = new HashMap<Integer, Integer>();
    }

    public static void PrintA(int[][] a) {
        //int result=0;
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                System.out.print(a[i][j] + ",");
            }
        }
        System.out.println();
        //return result;
    }

    public static void PrintA(int[] a) {
        //int result=0;
        for (int i = 0; i < a.length; i++) {

            System.out.print(a[i] + ",");

        }
        System.out.println();
    }
}

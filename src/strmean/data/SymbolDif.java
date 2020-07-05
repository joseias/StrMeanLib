package strmean.data;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import strmean.main.JConstants;

/**
 * Wraps the substitution matrix
 */
public class SymbolDif implements Serializable {

    private final char cIni = '0', cFin = '7';
    private final char _vacio = ' ';
    private char[][][] _masCercanos = null;

    protected float[][] cSW;
    protected float[] cIW;
    protected float[] cDW;

    protected HashMap<Character, Integer> symbolIndex;
    String[] symbols;

    public int c_AlphabetSize;

    public SymbolDif(Properties p) throws IOException {
        String cfgFilePath = p.getProperty(JConstants.WEIGHT_MATRIX_FILE);
        this.initW(cfgFilePath);
        c_AlphabetSize = symbolIndex.size();
    }

    /**
     * Substitution
     *
     * @param symbol1
     * @param symbol2
     * @return
     */
    public float sus(char symbol1, char symbol2) {
        int s1Index = this.symbolIndex.get(symbol1);
        int s2Index = this.symbolIndex.get(symbol2);

        return cSW[s1Index][s2Index];
    }

    public float ins(char symbol) {
        int indexS = this.symbolIndex.get(symbol);
        return cIW[indexS];
    }

    public float del(char symbol) {
        int indexS = this.symbolIndex.get(symbol);
        return cDW[indexS];
    }

    /**
     * *
     * Swap
     *
     * @param symbol1
     * @param symbol2
     * @return
     */
    public float swap(char symbol1, char symbol2) {
        return 0;
    }

    private char getNN1(char a, char b) {
        if (cIni <= a && a <= cFin && cIni <= b && b <= cFin) {
            return _masCercanos[0][a - cIni][b - cIni];
        } else {
            return _vacio;
        }
    }

    private char getNN2(char a, char b) {
        if (cIni <= a && a <= cFin && cIni <= b && b <= cFin) {
            return _masCercanos[1][a - cIni][b - cIni];
        } else {
            return _vacio;
        }
    }

    public ArrayList<Character> getNearestSymbols(char s1, char s2) {
        char c1 = getNN1(s1, s2);
        char c2 = getNN2(s1, s2);
        ArrayList<Character> result = new ArrayList<>(2);
        if (c1 != ' ') {
            result.add(c1);
        }
        if (c2 != ' ') {
            result.add(c2);
        }
        Collections.sort(result);

        if (result.size() > 1 && result.get(0) > result.get(1)) {
            System.err.println("Error SymbolDif");
        }
        return result;
    }

    /**
     *
     * @param WFilePath
     */
    protected final void initW(String WFilePath) {
        try {
            LineNumberReader lnr = new LineNumberReader(new FileReader(WFilePath));
            String actualLine = lnr.readLine();
            int total = Integer.parseInt(actualLine);

            /*[i,0] y [0,i] insertions and detetions respectively*/
            cSW = new float[total][total];
            cIW = new float[total];
            cDW = new float[total];
            actualLine = lnr.readLine();
            symbolIndex = new HashMap<>(total);
            String[] Symbols = actualLine.trim().split(" ");
            symbols = Symbols;

            /* create symbol index*/
            for (int i = 0; i < Symbols.length; i++) {
                symbolIndex.put(Symbols[i].charAt(0), i);
            }

            /* line with insertions costs for each symbol*/
            actualLine = lnr.readLine();
            Symbols = actualLine.trim().split(" ");
            for (int i = 0; i < Symbols.length; i++) {
                cIW[i] = Float.parseFloat(Symbols[i]);
            }

            /* lines with sustitutions and deletions cost in the first column*/
            int actualSymbol = 0;
            while ((actualLine = lnr.readLine()) != null && actualSymbol < total) {
                Symbols = actualLine.trim().split(" ");
                cDW[actualSymbol] = Float.parseFloat(Symbols[0]);

                for (int i = 1; i < Symbols.length; i++) {
                    cSW[actualSymbol][i - 1] = Float.parseFloat(Symbols[i]);
                }
                actualSymbol++;
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Score matrix format error...");
        }

    }

    /**
     * *
     * Revise this method to deal with non numeric symbols...
     */
    protected final void initNN() {
        float minActual;
        float aux;
        _masCercanos = new char[2][cFin + 1][cFin - cIni + 1];
        /*create a matrix of nearest symbols*/
        for (char i = cIni; i <= cFin; i++) {
            for (char j = cIni; j <= cFin; j++) {
                minActual = Integer.MAX_VALUE;

                /*compute distance to nearest symbols*/
                for (char car = cIni; car <= cFin; car++) {
                    aux = Math.max(sus(i, car), sus(car, j));
                    if (aux <= minActual) {
                        minActual = aux;
                    }//if
                }//for car

                int id = 0;
                _masCercanos[0][i - cIni][j - cIni] = _masCercanos[1][i - cIni][j - cIni] = _vacio;
                for (char car = cIni; car <= cFin; car++) {
                    aux = Math.max(sus(i, car), sus(car, j));
                    if (aux == minActual && id <= 1) {

                        _masCercanos[id++][i - cIni][j - cIni] = car;

                    }//if
                }//for car
            }//for j
        }//for i

    }

    public int getIndex(char symbol) {
        return symbolIndex.get(symbol);
    }

    public char getSymbol(int index) {
        return symbols[index].charAt(0);
    }

    public static void main(String[] args) throws IOException {
        /*Code to test SymbolDif*/
    }

}

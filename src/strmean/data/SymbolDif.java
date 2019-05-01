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
 * Wraps the sustitution matrix
 * 
 * @author jose
 */
public class SymbolDif implements Serializable
{
    private final char cIni = '0', cFin = '7';
    private final char _vacio = ' ';
    private char[][][] _masCercanos = null;
            
    protected float[][] cSW;
    protected float[]   cIW;
    protected float[]   cDW;
    
    protected HashMap<Character,Integer> symbolIndex;
    String[] symbols;
    
    public int c_AlphabetSize;
    
    public SymbolDif(Properties p) throws IOException{
        String m_cfgFilePath=p.getProperty(JConstants.WEIGHT_MATRIX_FILE);
        this.initW(m_cfgFilePath);
        c_AlphabetSize=symbolIndex.size();
    }

    /**
     * Sustitution
     * 
     * @param a_symbol1
     * @param a_symbol2
     * @return
     */
    public float sus(char a_symbol1,char a_symbol2){   
        int a_s1Index=this.symbolIndex.get(a_symbol1);
        int a_s2Index=this.symbolIndex.get(a_symbol2);
        
        return cSW[a_s1Index][a_s2Index];
    }
    
    public float ins(char a_symbol){
        int m_indexS=this.symbolIndex.get(a_symbol);
        return cIW[m_indexS];
    }
    
    public float del(char a_symbol){
        int m_indexS=this.symbolIndex.get(a_symbol);
        return cDW[m_indexS];
    }

    /***
     * Swap
     * @param a_symbol1
     * @param a_symbol2
     * @return 
     */
    public float swap(char a_symbol1, char a_symbol2){
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

    public ArrayList<Character> getNearestSymbols(char a_s1, char a_s2){
        char c1=getNN1(a_s1, a_s2);
        char c2=getNN2(a_s1, a_s2);
        ArrayList<Character> m_result=new ArrayList<>(2);
        if(c1!=' ') m_result.add(c1);
        if(c2!=' ')m_result.add(c2);
        Collections.sort(m_result);
        
        if(m_result.size()>1&&m_result.get(0)>m_result.get(1)){
            System.err.println("Error SymbolDif");
        }
        return m_result;
    }
    
    /**
     *
     * @param a_WFilePath
     */
    protected final void initW(String a_WFilePath){  
        try{
            LineNumberReader m_lnr=new LineNumberReader(new FileReader(a_WFilePath));
            String m_actualLine=m_lnr.readLine();
            int m_total=Integer.parseInt(m_actualLine);
            
            /*[i,0] y [0,i] insercion y borrado respectivamente*/
            cSW=new float[m_total][m_total];
            cIW=new float[m_total];
            cDW=new float[m_total];
            m_actualLine=m_lnr.readLine();
            symbolIndex=new HashMap<>(m_total);
            String[] m_Symbols=m_actualLine.trim().split(" ");
            symbols=m_Symbols;
            
            /* Create symbol index*/
            for(int i=0;i<m_Symbols.length;i++){
               symbolIndex.put(m_Symbols[i].charAt(0),i);
            }
            
            /* Line with insertions costs for each symbol*/
            m_actualLine=m_lnr.readLine();
            m_Symbols=m_actualLine.trim().split(" ");
            for(int i=0;i<m_Symbols.length;i++){
                cIW[i]=Float.parseFloat(m_Symbols[i]);
            }
            
            /* Lines with sustitutions and deletions cost in the first column*/
            int m_actualSymbol=0;
            while((m_actualLine=m_lnr.readLine())!=null && m_actualSymbol<m_total){
               m_Symbols=m_actualLine.trim().split(" ");
               cDW[m_actualSymbol]=Float.parseFloat(m_Symbols[0]);
               
               for(int i=1;i<m_Symbols.length;i++)
               {
                   cSW[m_actualSymbol][i-1]=Float.parseFloat(m_Symbols[i]);
               }
               m_actualSymbol++;
            }
        }
        catch(IOException | NumberFormatException m_e){
          System.err.println("Score matrix format error...");
        }
        
    }
    
    /***
     *  Revise this method to deal with non numeric symbols...
     */
    protected final void initNN(){
         float minActual;
         float aux;
        _masCercanos = new char[2][cFin + 1][cFin - cIni + 1];
        //Crear una matriz de los más cercanos
        for (char i = cIni; i <= cFin; i++) {
            for (char j = cIni; j <= cFin; j++) {
                minActual = Integer.MAX_VALUE;

                //Buscar la distancia de los más cercanos
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
    
    public int getIndex(char a_symbol){
        return symbolIndex.get(a_symbol);
    }
  
    public char getSymbol(int index){    
        return symbols[index].charAt(0);
    }
    
    public static void main(String[] args) throws IOException{
        /*Code to test SymbolDif*/
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import strmean.data.Example;

/**
 *
 * @author jabreu
 */
public final class JUtils 
{
     public static String getArgsType(String a_fpath,String a_argName)throws IOException {
         String m_result="";
         LineNumberReader m_lnr=new LineNumberReader(new FileReader(a_fpath));
         String m_actualLine=m_lnr.readLine();
         String[] m_tokens;
         while(m_actualLine!=null){
             m_tokens=m_actualLine.split("=");
             if(m_tokens[0].equals(a_argName)){
                 m_result=m_tokens[1];
                 break;
             }
             m_actualLine=m_lnr.readLine();
         }
         return m_result;
     }
     
     public static Object buildInstanceFromType (String a_type){
         try{
          Object m_result;
          Class m_sDC=Class.forName(a_type);
          m_result=m_sDC.newInstance();
          return m_result;
         }
         catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
             return null;
         }
     }
     
     
    public static Object buildInstanceFromType (String a_type, Properties p){
         try{
          Class<?> cl = Class.forName(a_type);
          Constructor<?> cons = cl.getConstructor(Properties.class);
          Object m_result = cons.newInstance(p);
          return m_result;
         }
         catch(ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e){
             return null;
         }
    }
     
     /***
      * Load a set of instances, could be in one of the following formats:
      * [class][space][sequence] or 
      * [sequence]
      * @param a_epath
      * @return
      * @throws Exception 
      */
     public static List<Example> loadExamples(String a_epath) throws Exception
     {
         
         List<Example> m_result=new ArrayList<>();
         try{
             LineNumberReader m_lnr=new LineNumberReader(new FileReader(a_epath));
             String m_actualLine=m_lnr.readLine();
             String[] m_tokens;
             Example tmp = null;
             int id = 1;
             while(m_actualLine!=null)
             {
                 m_tokens=m_actualLine.split(" ");
                 if(m_tokens.length==1){
                    tmp = new Example("-",m_tokens[0]);
                 }
                 else{
                    tmp = new Example(m_tokens[0],m_tokens[1]); 
                 }
                 
                 if(tmp.ID == null){
                     tmp.ID = Integer.toString(id);
                     id++;
                 }
                 
                 m_result.add(tmp);
                 m_actualLine=m_lnr.readLine();
             
             }
         }
         catch(Exception m_e){
             System.err.println("Please verify format of instances in "+a_epath);
         }
         return m_result;
     }
  
     public static Properties loadProperties(){
         try {
            Properties p=new Properties();
            p.load(new FileReader(JConstants.PROPERTIES_FILE));
            return p;
         } 
         catch (Exception e) {
             return null;
         }
     }
     
     public static void initLogger(){
         try{
                File file = new File("error.log");
                FileOutputStream fos = new FileOutputStream(file,true);
                PrintStream ps = new PrintStream(fos);
                System.setErr(ps);	

                Date date = new Date();
                String strDateFormat = "EEE, d MMM yyyy HH:mm:ss";
                DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
                String formattedDate= dateFormat.format(date);
                System.err.println("");
                System.err.println("============ Log of execution "+ formattedDate + "============");
         }
         catch(FileNotFoundException e){
                /*Default logger*/
                Date date = new Date();
                String strDateFormat = "EEE, d MMM yyyy HH:mm:ss";
                DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
                String formattedDate= dateFormat.format(date);
                System.err.println("");
                System.err.println("============ Log of execution "+ formattedDate + "============");
         }
     }
}

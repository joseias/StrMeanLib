package strmean.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import strmean.data.Example;

public final class JUtils {

    public static String getArgsType(String fpath, String argName) throws IOException {
        String result = "";
        LineNumberReader lnr = new LineNumberReader(new FileReader(fpath));
        String actualLine = lnr.readLine();
        String[] tokens;
        while (actualLine != null) {
            tokens = actualLine.split("=");
            if (tokens[0].equals(argName)) {
                result = tokens[1];
                break;
            }
            actualLine = lnr.readLine();
        }
        return result;
    }

    /**
     * Create a new instance of the given class.
     *
     * @param <T> target type
     * @param type the target type
     * @param className the class to create an instance of
     * @return the new instance
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static <T> T newInstance(Class<? extends T> type, String className) throws ReflectiveOperationException {

        Class<?> clazz = Class.forName(className);
        Class<? extends T> targetClass = clazz.asSubclass(type);
        T result = targetClass.getDeclaredConstructor().newInstance();
        return result;
    }

    /**
     * Create a new instance of the given class.
     *
     * @param <T> target type
     * @param type the target type
     * @param className the class to create an instance of
     * @param p
     * @return the new instance
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws java.lang.NoSuchMethodException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static <T> T newInstance(Class<? extends T> type, String className, Properties p) throws ReflectiveOperationException {

        Class<?> clazz = Class.forName(className);
        Class<? extends T> targetClass = clazz.asSubclass(type);
        Constructor<?> cons = targetClass.getConstructor(Properties.class);
        T result = (T) cons.newInstance(p);
        return result;
    }

    /**
     * *
     * Load a set of instances, could be in one of the following formats:
     * [class][space][sequence] or [sequence]
     *
     * @param epath
     * @return
     * @throws IOException
     */
    public static List<Example> loadExamples(String epath) throws IOException {

        List<Example> result = new ArrayList<>();
        LineNumberReader lnr = new LineNumberReader(new FileReader(epath));
        String actualLine;
        String[] tokens;
        Example tmp = null;
        int id = 1;
        while ((actualLine = lnr.readLine()) != null) {
            tokens = actualLine.split(" ");
            if (tokens.length == 1) {
                tmp = new Example("-", tokens[0]);
            } else {
                tmp = new Example(tokens[0], tokens[1]);
            }

            if (tmp.ID == null) {
                tmp.ID = Integer.toString(id);
                id++;
            }

            result.add(tmp);
        }
        return result;
    }

    public static Properties loadProperties() {
        try {
            Properties p = new Properties();
            p.load(new FileReader(JConstants.PROPERTIES_FILE));
            return p;
        } catch (IOException e) {
            return null;
        }
    }

    public static void initLogger() {
        String ERROR_LOG_FILE = "error.log";
        try {
            File file = new File(ERROR_LOG_FILE);
            FileOutputStream fos = new FileOutputStream(file, true);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);

            Date date = new Date();
            String strDateFormat = "EEE, d MMM yyyy HH:mm:ss";
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
            String formattedDate = dateFormat.format(date);
            System.err.println("");
            System.err.println("============ Log of execution " + formattedDate + "============");
        } catch (FileNotFoundException e) {
            /*Default logger*/
            System.err.println("Could'nt intialized default error log file " + ERROR_LOG_FILE + "... using System.err ...");
            Date date = new Date();
            String strDateFormat = "EEE, d MMM yyyy HH:mm:ss";
            DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
            String formattedDate = dateFormat.format(date);
            System.err.println("");
            System.err.println("============ Log of execution " + formattedDate + "============");
        }
    }
}

package strmean.main;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class JMathUtils {

    public static float min(float a, float b, float c) {
        if (a < b && a < c) {
            return a;
        } else if (b < c) {
            return b;
        } else {
            return c;
        }
    }

    public static float fmin(float a, float b, float c) {
        if (a < b && a < c) {
            return a;
        } else if (b < c) {
            return b;
        } else {
            return c;
        }
    }

    public static double getStdv(float[] values, double median) {
        double variance = 0;
        int cant = values.length;

        for (int i = 0; i < cant; i++) {
            variance = variance + Math.pow(values[i] - median, 2);
        }

        return Math.sqrt(variance);
    }

    public static double round(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal iset = new BigDecimal(value);
        iset = iset.setScale(precision, RoundingMode.HALF_UP);
        return iset.doubleValue();
    }
}

package model.data;

public class StatLib {
    //names of methods say it all
    public static float avg(float[] x) {
        float sum = 0;
        for (float var : x) {
            sum += var;
        }
        return sum / (x.length);
    }

    public static float var(float[] x) {
        float sum = 0, avgPow2, average;
        for (float var : x) {
            sum += var * var;
        }
        avgPow2 = sum / x.length;
        average = avg(x);
        return avgPow2 - (average * average);
    }

    public static float cov(float[] x, float[] y) {
        int size = x.length;
        float sum = 0, avgX=avg(x), avgY=avg(y);
        for (int i = 0; i < size; i++) {
            sum += (x[i] - avgX) * (y[i] - avgY);
        }
        return sum / size;
    }

    public static float pearson(float[] x, float[] y) {
        float standardDeviationX;
        float standardDeviationY;
        standardDeviationX = (float) Math.sqrt(var(x));
        standardDeviationY = (float) Math.sqrt(var(y));
        if (standardDeviationX == 0 || standardDeviationY == 0) {
            return Float.MIN_VALUE;
        }
        return cov(x, y) / (standardDeviationX * standardDeviationY);
    }

    public static Line linear_reg(Point[] points) {
        int size = points.length;
        float cov = 0,varX = 0,a,b,avgX = 0,avgY = 0;
        float[] arrx = new float[size];
        float[] arry = new float[size];
        for (int i = 0; i < size; i++) {
            arrx[i] = points[i].x;
            arry[i] = points[i].y;
        }
        cov = cov(arrx, arry);
        varX += var(arrx);
        a = cov / varX;
        for (Point p : points) {
            avgX += p.x;
            avgY += p.y;
        }
        avgX /= size;
        avgY /= size;
        b = avgY - (avgX * a);
        return new Line(a, b);
    }

    public static float dev(Point p, Point[] points) {
        Line l = linear_reg(points);
        float y = 0;
        y = (p.x * l.a) + l.b;
        return Math.abs(y - p.y);
    }

    public static float dev(Point p, Line l) {
        float y = 0;
        y = (p.x * l.a) + l.b;
        return Math.abs(y - p.y);
    }
}

package model.algorithems;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import model.data.CorrelatedFeatures;

public class HelperUtility {
    //returns the highest correlated value in a column list
    public static CorrelatedFeatures getMaxColumnCorrelatedFeature(String value, List<CorrelatedFeatures> correlatedFeatures) {
        CorrelatedFeatures result = null;
        if (correlatedFeatures != null) {
            for (CorrelatedFeatures ptr : correlatedFeatures) {
                if (ptr.valA.equalsIgnoreCase(value)) {
                    if (result == null) {
                        result = ptr;
                    } else {
                        if (ptr.correlation > result.correlation) {
                            result = ptr;
                        }
                    }
                }
            }
        }
        return result;
    }

    //loads a class through a defined search string
    public static TimeSeriesAnomalyDetector loadPlugin(String directory, String className) {
        try {
            URLClassLoader urlClassLoader = URLClassLoader.newInstance(new URL[]{new URL("file://" + directory)});
            Class<?> c = urlClassLoader.loadClass("model.algorithems." + className);
            return (TimeSeriesAnomalyDetector) c.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}

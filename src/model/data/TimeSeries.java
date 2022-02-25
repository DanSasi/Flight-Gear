package model.data;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class TimeSeries {
    private HashMap<Integer, String> Features = new HashMap<>();
    private List<String> listFeaturesNames = new ArrayList<>();

    private HashMap<String, ArrayList<Float>> data = new HashMap<>();

    private ArrayList<ArrayList<Float>> rows = new ArrayList<ArrayList<Float>>();

    private int size;
    //reads all data from the csv file and puts the in a map
    //the columns names are saved in a list
    public TimeSeries(String csvFileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFileName));
            String line;
            if ((line = br.readLine()) != null) {
                int i = 0;
                String[] values = line.split(",");
                for (String v : values) {
                    if (Features.containsValue(v)) {
                        String newName = v + "1";
                        Features.put(i++, newName);
                        listFeaturesNames.add(newName);
                    } else {
                        Features.put(i++, v);
                        listFeaturesNames.add(v);
                    }
                }
            }
            while ((line = br.readLine()) != null)
            {
                int i = 0;
                String[] values = line.split(",");
                ArrayList<Float> floats = new ArrayList<>();
                for (String v : values) {
                    String featureName = Features.get(i);
                    data.computeIfAbsent(featureName, k -> new ArrayList<Float>());
                    data.get(featureName).add(Float.parseFloat(v));
                    i++;
                    floats.add(Float.parseFloat(v));
                }
                rows.add(floats);
                size++;
            }
        } catch (Exception ignore) { }
    }
    //returns a column name according to its index
    public String GetFeatureName(Integer index) {
        return Features.get(index);
    }
    //returns the whole list of the columns names
    public List<String> GetAllFeaturesNames() {
        return listFeaturesNames;
    }
    //gets index of the column name
    public int GetIndex(String feature) {
        for (Map.Entry<Integer, String> integerStringEntry : Features.entrySet()) {
            Map.Entry pair = (Map.Entry) integerStringEntry;
            if (pair.getValue().equals(feature)) {
                return (int) pair.getKey();
            }
        }
        return 0;
    }
    //num of columns
    public int GetAmountOfFeatures() {
        return Features.size();
    }
    //num of all values in the csv
    public int GetAmountOfAllFeatures() {
        return size;
    }
    //gets all the values of a column
    public float[] getArrayFromString(String name) {
        List<Float> values = data.get(name);
        float[] floatArray = new float[values.size()];
        int i = 0;

        for (Float f : values) {
            floatArray[i++] = (f != null ? f : Float.NaN);
        }
        return floatArray;
    }
    //gets values to a certain index from the col array
    public float[] getArrayFromStringUntilTimeStamp(String name, int timeStamp) {
        List<Float> values = data.get(name);
        if (values == null) {
            return new float[0];
        }
        float[] floatArray = new float[timeStamp];
        int i = 0;
        for (Float f : values) {
            if (i == timeStamp) {
                break;
            }
            floatArray[i++] = (f != null ? f : Float.NaN);
            if (i == timeStamp) {
                break;
            }
        }
        return floatArray;
    }
    //returns col according to the index
    public float[] GetListFromIndex(Integer index) {
        String nameOfFeature = Features.get(index);
        return getArrayFromString(nameOfFeature);
    }
    //returns a certain row from the table
    public float[] GetRow(Integer line) {
        ArrayList<Float> currentLine = rows.get(line);
        float[] floatArray = new float[currentLine.size()];
        int i = 0;

        for (Float f : currentLine) {
            floatArray[i++] = (f != null ? f : Float.NaN);
        }
        return floatArray;
    }
    //returns a col according to string but returns an array list
    public ArrayList<Float> GetDataThroughString(String s) {
        return data.get(s);
    }

}

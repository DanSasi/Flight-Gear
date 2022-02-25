package model.data;


import model.algorithems.SimpleAnomalyDetector;
import model.algorithems.TimeSeriesAnomalyDetector;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Commands {

    public static abstract class Command {
        protected String value;
        protected String description;
        protected DefaultIO defaultIO;

        public Command(DefaultIO defaultIO, String description) {
            this.description = description;
            this.defaultIO = defaultIO;
        }

        public abstract void execute();

        public String getDescription() {
            return description;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setOutput(DefaultIO defaultIO) {
            this.defaultIO = defaultIO;
        }
    }

    public interface DefaultIO {

        public int indexToPull = 0;
        public List<String> csvPath = new ArrayList<>();
        public List<AnomalyReport> anomalyReports = new ArrayList<>();

        public String readText();

        public void write(String text);

        public float readVal();

        public void write(float val);

        public default void uploadFile(String path){
            csvPath.add(path);
        }

        public default String downloadFile(int i ){
            return csvPath.get(i);
        }

        public default void saveLastResults(List<AnomalyReport> anomalyReports){
            DefaultIO.anomalyReports.addAll(anomalyReports);
        }

        public default List<AnomalyReport> getLastResults(){
            return anomalyReports;
        }

    }

    DefaultIO dio;

    public Commands(DefaultIO dio) {
        this.dio = dio;
    }

    class SharedState { private SharedState sharedState = new SharedState();}

    public static class WelcomeCommand extends Command {

        public WelcomeCommand() {
            super(null, "Welcome to the Anomaly Detection Server.\nPlease choose an option:");
        }

        @Override
        public void execute() { }
    }

    public static class Exit extends Command {

        public Exit(DefaultIO defaultIO) {
            super(defaultIO, "exit");
        }

        @Override
        public void execute() { }
    }

    public static class AlgSettings extends Command {
        private float defaultCorrelation = 0.9f;
        public AlgSettings(DefaultIO defaultIO) {
            super(defaultIO, "algorithm settings");
        }

        @Override
        public void execute() {
            defaultIO.write(String.format("The current correlation threshold is %.2f", defaultCorrelation));
            boolean isValidInput;
            do {
                defaultIO.write("Type a new threshold");
                try {
                    float realChoice = Float.parseFloat(defaultIO.readText());
                    if (realChoice < 0 || realChoice > 1) {
                        isValidInput = false;
                    } else {
                        isValidInput = true;
                        defaultCorrelation = realChoice;
                    }
                } catch (Exception e) {
                    isValidInput = false;
                }
                if (!isValidInput) {
                    defaultIO.write("please choose a value between 0 and 1");
                }
            } while (!isValidInput);
        }
    }

    public static class DetectAnomalies extends Command {

        public DetectAnomalies(DefaultIO defaultIO) {
            super(defaultIO, "detect anomalies");
        }

        @Override
        public void execute() {
            TimeSeries ts = new TimeSeries(defaultIO.downloadFile(0));
            TimeSeriesAnomalyDetector simpleAnomalyDetector = new SimpleAnomalyDetector();
            simpleAnomalyDetector.learnNormal(ts);
            TimeSeries ts2 = new TimeSeries(defaultIO.downloadFile(1));
            defaultIO.saveLastResults(simpleAnomalyDetector.detect(ts2));
            defaultIO.write("anomaly detection complete.");
        }
    }

    public static class DisplayResults extends Command {

        public DisplayResults(DefaultIO defaultIO) {
            super(defaultIO, "display results");
        }

        @Override
        public void execute() {
            List<AnomalyReport> anomalyReportList = defaultIO.getLastResults();
            if (anomalyReportList != null) {
                for (AnomalyReport anomalyReport : anomalyReportList) {
                    defaultIO.write(String.format("%d     %s", anomalyReport.timeStep, anomalyReport.description));
                }
            }
            defaultIO.write("Done.");
        }
    }

    public static class UploadAnomaliesAndAnalyze extends Command {

        public UploadAnomaliesAndAnalyze(DefaultIO defaultIO) {
            super(defaultIO, "upload anomalies and analyze results");
        }

        @Override
        public void execute() {
            defaultIO.write("Insert path file name");
            defaultIO.write("below:");
            TimeSeries timeSeries = new TimeSeries(defaultIO.downloadFile(0));
            List<String> list = getExceptionLinesFromFile();
            numberOfStepsContinuation(defaultIO.getLastResults());
            float P = list.size();

            float N = timeSeries.GetAmountOfAllFeatures() - calculateTheSumOfStepsFromFile(list);
            List<String> rangeList = numberOfStepsContinuation(defaultIO.getLastResults());
            float FP = calculateFromOutOfRange(rangeList, list);
            float TP = calculateInRange(rangeList, list);

            defaultIO.write(String.format("True Positive Rate: %s", formatMyNumber(TP / P)));
            defaultIO.write(String.format("False Positive Rate: %s", formatMyNumber(FP / N)));
        }

        private String formatMyNumber(float num) {
            String realNumber = String.valueOf(num);
            char[] ch = new char[realNumber.length()];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < realNumber.length(); i++) {
                ch[i] = realNumber.charAt(i);
            }
            for (int i = 0; i < ch.length; i++) {
                if (i == 4 && ch[i] == '0') {
                    break;
                } else if (i == 4) {
                    sb.append(ch[i]);
                    break;
                } else {
                    sb.append(ch[i]);
                }

            }
            return sb.toString();
        }

        private float calculateInRange(List<String> anomalyReports, List<String> list) {
            int count = 0;
            if (anomalyReports != null) {
                for (String anomalyReport : anomalyReports) {
                    String[] anomalySplit = anomalyReport.split(",");
                    int left = Integer.parseInt(anomalySplit[0]);
                    int right = Integer.parseInt(anomalySplit[1]);
                    for (String ptr : list) {
                        String[] splitArrayFromFile = ptr.split(",");
                        int leftFromFile = Integer.parseInt(splitArrayFromFile[0]);
                        int rightFromFile = Integer.parseInt(splitArrayFromFile[1]);
                        int resultOfCut = FindOverlapping(left, right, leftFromFile, rightFromFile);
                        if (resultOfCut > 0) {
                            count++;
                            break;
                        }
                    }
                }
            }
            return count;
        }

        private float calculateFromOutOfRange(List<String> anomalyReports, List<String> list) {
            int count = 0;
            if (anomalyReports != null) {
                for (String anomalyReport : anomalyReports) {
                    String[] anomalySplit = anomalyReport.split(",");
                    int left = Integer.parseInt(anomalySplit[0]);
                    int right = Integer.parseInt(anomalySplit[1]);
                    boolean flag = false;
                    for (String ptr : list) {
                        String[] splitArrayFromFile = ptr.split(",");
                        int leftFromFile = Integer.parseInt(splitArrayFromFile[0]);
                        int rightFromFile = Integer.parseInt(splitArrayFromFile[1]);
                        int resultOfCut = FindOverlapping(left, right, leftFromFile, rightFromFile);
                        if (resultOfCut == 0) { } else {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) { count++;}
                }
            }
            return count;
        }

        private int calculateTheSumOfStepsFromFile(List<String> resultFromFile) {
            int sum = 0;
            if (resultFromFile != null) {
                for (String ptr : resultFromFile) {
                    String[] splitArray = ptr.split(",");
                    int left = Integer.parseInt(splitArray[0]);
                    int right = Integer.parseInt(splitArray[1]);
                    sum += right - left + 1;
                }
            }
            return sum;
        }

        private List<String> getExceptionLinesFromFile() {
            String line;
            List<String> lines = new ArrayList<>();
            do {
                line = defaultIO.readText();
                if (!line.equals("done")) {
                    lines.add(line);
                }
            } while (!line.equals("done"));
            return lines;
        }

        private List<String> numberOfStepsContinuation(List<AnomalyReport> list) {

            List<String> rangeList = new ArrayList<>();
            CopyOnWriteArrayList<AnomalyReport> copy = new CopyOnWriteArrayList<>(list);
            int count = 0;
            String startRange = null;
            float startRangeFloat = 0;
            if (list.size() > 0) {
                for (int i = 0; i < copy.size() - 1; i++) {
                    if (copy.get(i).description.equals(copy.get(i + 1).description) && copy.get(i).timeStep + 1 == copy.get(i + 1).timeStep) {
                        count++;
                        if (startRange == null) {
                            startRange = String.valueOf(copy.get(i).timeStep);
                            startRangeFloat = copy.get(i).timeStep;
                        }
                        if (i == copy.size() - 2) {
                            int result = (int) (startRangeFloat + count);
                            startRange += "," + result;
                            rangeList.add(startRange);
                        }
                    } else {
                        int result = (int) (startRangeFloat + count);
                        startRange += "," + result;
                        rangeList.add(startRange);
                        startRange = null;
                        count = 0;
                    }
                }
            }
            return rangeList;
        }

        int FindOverlapping(int start1, int end1, int start2, int end2) {
            return Math.max(0, Math.min(end1, end2) - Math.max(start1, start2) + 1);
        }
    }

    public static class UploadTimeSeriesCsvFile extends Command {
        private static final String name = "csvFiles";

        public UploadTimeSeriesCsvFile(DefaultIO defaultIO) {
            super(defaultIO, "upload a csv file");
        }

        @Override
        public void execute() {
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    defaultIO.write("Please upload your local train CSV file.");
                } else {
                    defaultIO.write("Please upload your local test CSV file.");
                }
                String line = null;
                String fileName = name + i;
                try (FileWriter fileWriter = new FileWriter(fileName)) {
                    do {
                        line = defaultIO.readText();
                        if (!line.equals("done")) {
                            fileWriter.write(line + "\n");
                        }

                    } while (!line.equals("done"));
                    fileWriter.close();
                    defaultIO.write("Upload complete.");
                    defaultIO.uploadFile(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package model.player;

import model.data.TimeSeries;
import model.settings.Settings;
import view.ChangeDataListener;
import view.ChangeTimeUIListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Player {

    private TimeSeries ts;
    private Settings settings;
    private final static int MILISECONDS = 1000;
    private int curTimeStamp = 0;
    private int secs = 0;
    private int ratio;
    private int length;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture<?> scheduledFuture = null;
    private final List<ChangeDataListener> changeDataListeners = new ArrayList<>();
    private final List<ChangeTimeUIListener> uiListeners = new ArrayList<>();

    //initializes the time series and the vid len
    public void setTimeSeries(TimeSeries ts) {
        this.ts = ts;
        length = ts.GetAmountOfAllFeatures() / ratio;
    }
    //sets the settings and the play ratio
    public void setSettings(Settings settings) {
        this.settings = settings;
        ratio = MILISECONDS / settings.getDataSamplingRate();
    }
    //adds a listener to the player
    public void addChangeDataListener(ChangeDataListener changeDataListener) {
        changeDataListeners.add(changeDataListener);
    }
    //adds a listener to a time skip
    public void addTimeChangeListener(ChangeTimeUIListener changeTimeUIListener) {
        uiListeners.add(changeTimeUIListener);
    }
    //plays at the desired rate and prints time stamps in the terminal
    public void play() {
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Playing time stamp - " + curTimeStamp);
            setTimeStamp(curTimeStamp + 1);
            if (curTimeStamp % ratio == 0) {
                setClock(secs + 1);
            }

        }, 0, settings.getDataSamplingRate(), TimeUnit.MILLISECONDS);

    }
    //pauses the video
    public void pause() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
    //returns the playback to a certain time stamp
    public void setTimeStamp(int timeStamp) {
        curTimeStamp = timeStamp;
        float[] data = ts.GetRow(curTimeStamp);
        for (ChangeDataListener changeDataListener : changeDataListeners) {
            changeDataListener.onChangedData(curTimeStamp, data);
        }
    }
    //changes the clock according to the time stamp
    public void setClock(int seconds) {
        secs = seconds;
        for (ChangeTimeUIListener changeTimeUIListener : uiListeners) {
            changeTimeUIListener.onChangedTime(seconds);
        }
    }
    //stops the video
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        setClock(0);
        setTimeStamp(0);
    }

    public int getLength() {
        return ts.GetAmountOfAllFeatures();
    }
    //skips to a certain part of a vid
    public void injectTimeStamp(int value) {
        setTimeStamp(value);
        setClock(value / ratio);
    }
    //gets the time passed so far in the vid
    public int getCurrentTimeStamp() {
        return curTimeStamp;
    }
}

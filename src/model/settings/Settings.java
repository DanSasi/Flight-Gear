package model.settings;

import org.w3c.dom.Document;

import java.util.List;

public class Settings {
    private static final String DATA_SAMPLING_RATE_TAG = "DataSamplingRate";
    private static final String FLIGHT_GEAR_PORT = "FlightGearPort";
    private List<Property> propertyList;
    private int dataSamplingRate;
    private int flightGearPort;

    public static Settings fromDoc(Document doc) {
        Settings settings = new Settings();
        settings.setPropertyList(PropertyList.fromDoc(doc));
        settings.setDataSamplingRate(Integer.parseInt(doc.getElementsByTagName(DATA_SAMPLING_RATE_TAG).item(0).getTextContent().trim()));
        settings.setFlightGearPort(Integer.parseInt(doc.getElementsByTagName(FLIGHT_GEAR_PORT).item(0).getTextContent().trim()));
        return settings;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    private void setPropertyList(List<Property> list) {
        propertyList = list;
    }

    public int getDataSamplingRate() {
        return dataSamplingRate;
    }

    public void setDataSamplingRate(int mDataSamplingRate) {
        this.dataSamplingRate = mDataSamplingRate;
    }

    public int getFlightGearPort() { return flightGearPort; }

    public void setFlightGearPort(int port) {
        flightGearPort = port;
    }
}

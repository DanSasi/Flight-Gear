package model.settings;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Property {

    private String name;
    private int columnNumber;
    private int minRange;
    private int maxRange;
    public static String TAG_ELEMENT = "Property";
    public static String TAG_NAME = "name";
    public static String TAG_COLUMN_NUMBER = "columNumber";
    public static String TAG_MIN_RANGE = "minRange";
    public static String TAG_MAX_RANGE = "maxRange";

    public static Property fromDoc(Node item) {
        Property property = new Property();
        Element actualItem = (Element) item;
        String name = actualItem.getElementsByTagName(TAG_NAME).item(0).getTextContent();
        int columNumber = Integer.parseInt(actualItem.getElementsByTagName(TAG_COLUMN_NUMBER).item(0).getTextContent());
        int maxRange = Integer.parseInt(actualItem.getElementsByTagName(TAG_MAX_RANGE).item(0).getTextContent());
        int minRange = Integer.parseInt(actualItem.getElementsByTagName(TAG_MIN_RANGE).item(0).getTextContent());
        property.setColumnNumber(columNumber);
        property.setMaxRange(maxRange);
        property.setMinRange(minRange);
        property.setName(name);
        return property;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getMinRange() {
        return minRange;
    }

    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

}

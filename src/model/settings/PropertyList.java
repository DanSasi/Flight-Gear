package model.settings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PropertyList {

    public static String TAG_ELEMENT = "PropertyList";

    public static List<Property> fromDoc(Document doc) {
        List<Property> propertyList = new ArrayList<>();
        Element element = (Element) doc.getElementsByTagName(PropertyList.TAG_ELEMENT).item(0);
        NodeList list = element.getElementsByTagName(Property.TAG_ELEMENT);
        for (int i = 0; i < list.getLength(); i++) {
            propertyList.add(Property.fromDoc(list.item(i)));
        }
        return propertyList;
    }
}

package model.settings;

import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlightGearSettingsReader {
    public static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    public static Settings getUserSettings(String fileName) throws Exception {
        Document doc = openXmlUserSettings(fileName);
        Settings settings = Settings.fromDoc(doc);
        FlightGearSettingsReader.CacheUserSettings(fileName);
        return settings;
    }

    public static Settings getCachedUserSettings() {
        Settings settings = null;
        try {
            Document doc = openXmlUserSettings("./resources/CachedSettings.xml");
            settings = Settings.fromDoc(doc);
        } catch (Exception e) { }
        return settings;
    }

    public static void CacheUserSettings(String fileName) {
        FileInputStream instream = null;
        FileOutputStream outstream = null;
        try {
            File infile = new File(fileName);
            File outfile = new File("./resources/CachedSettings.xml");
            instream = new FileInputStream(infile);
            outstream = new FileOutputStream(outfile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = instream.read(buffer)) > 0) {
                outstream.write(buffer, 0, length);
            }
            instream.close();
            outstream.close();
            System.out.println("File loaded successfully!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static Document openXmlUserSettings(String fileName) throws Exception {
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(fileName));
        doc.getDocumentElement().normalize();
        return doc;
    }
}
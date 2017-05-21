package com.example.maxim.mxconverter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class XMLParser {

    private static final String ns = null;

    public currList parse(InputStream in) throws XmlPullParserException, IOException {
        ArrayList<Curren> allCurr = new ArrayList<>();
        String date = "", name = "";
        Curren currency = null;
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(in, null);//"windows-1251"
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_TAG) {
                name = parser.getName();
                if (name.contentEquals("ValCurs")) {
                    date = parser.getAttributeValue(ns, "Date");
                } else if (name.contentEquals("Valute")) {
                    currency = new Curren();
                } else if (currency != null) {
                    if (name.contentEquals("NumCode")) {
                        currency.ncode = parser.nextText();
                    } else if (name.contentEquals("CharCode")) {
                        currency.scode = parser.nextText();
                    } else if (name.contentEquals("Name")) {
                        currency.name = parser.nextText();
                    } else if (name.contentEquals("Value")) {
                        String v = parser.nextText().replace(",", ".");
                        currency.value = Float.parseFloat(v);
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().contentEquals("Valute")) {
                    allCurr.add(currency);
                }
            }
            eventType = parser.next();

        }
        return new currList(allCurr, date);

    }


}

package ru.euphoria.doggy.api;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by admin on 08.04.18.
 */

public class FoafParser {

    public static String parse(String xml) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(xml));

        while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                if (parser.getName().equals("created")) {
                    return parser.getAttributeValue(0);
                }
            }

            parser.next();
        }
        throw new IOException("Can't find registration date");
    }
}

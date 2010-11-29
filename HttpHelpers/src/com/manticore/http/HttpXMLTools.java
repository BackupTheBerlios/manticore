/*
 *
 *  Copyright (C) 2010 Andreas Reichel <andreas@manticore-projects.com>
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package com.manticore.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import org.dom4j.*;
import org.dom4j.io.*;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpXMLTools {

    private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9\\,\\.\\+\\-]*)", Pattern.UNICODE_CASE);

    public static Document readXML(String Filename) throws DocumentException, MalformedURLException, IOException {
        Document document = null;
        SAXReader saxReader = new SAXReader(false);
        saxReader.setEncoding("UTF-8");

        URL url = new URL(Filename);
        if (url.getProtocol().equalsIgnoreCase("file")) {
            document = saxReader.read(Filename);
        } else {
            DefaultHttpClient client = HttpClientFactory.getClient();
            HttpGet get = new HttpGet(Filename);
            HttpResponse response = client.execute(get);
            document = saxReader.read(response.getEntity().getContent());
            get.abort();
        }

        return document;
    }

    public static Document parseHtml(String UrlStr) throws SAXException, DocumentException, IOException {
        String ParserClassname = "org.ccil.cowan.tagsoup.Parser";
        String NamespaceStr = "http://www.w3.org/1999/xhtml";
        Document document = null;

        // use tagsoup-parser
        XMLReader parser = XMLReaderFactory.createXMLReader(ParserClassname);
        SAXReader saxReader = new SAXReader(parser, false);
        saxReader.setEncoding("UTF-8");

        DefaultHttpClient client = HttpClientFactory.getClient();
        HttpGet get = new HttpGet(UrlStr);

        HttpResponse response = client.execute(get);
        InputStream inStream = response.getEntity().getContent();
        document = saxReader.read(inStream);
        get.abort();

        return document;
    }
}

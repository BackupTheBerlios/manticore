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
import com.manticore.http.HttpClientFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ListIterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.*;
import org.dom4j.io.*;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class XMLTools1 {

    private static final Pattern DECIMAL_NUMBER_PATTERN = Pattern.compile("([0-9\\,\\.\\+\\-]*)", Pattern.UNICODE_CASE);

    public static long writeToXML(Document doc, String filename) {
        return writeToXML(doc, new File(filename));
    }

    public static String writeTempXMLFile(Document doc) {
        String filename = "";
        try {
            File file = File.createTempFile("manticore", ".xml");
            writeToXML(doc, file);
            filename = file.getCanonicalPath();

        } catch (IOException ex) {
            Logger.getLogger(XMLTools1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filename;
    }

    public static long writeToXML(Document doc, File file) {
        long errorcode = 0;

        try {
            OutputFormat outformat = OutputFormat.createPrettyPrint();
            outformat.setEncoding("UTF-8");
            FileOutputStream out = new FileOutputStream(file);
            XMLWriter writer = new XMLWriter(out, outformat);
            writer.write(doc);
            writer.flush();
        } catch (Exception x) {
            System.out.println(x.getMessage());
            errorcode = x.hashCode();
        }
        return errorcode;
    }

    public static Document readXML(String Filename) throws DocumentException, MalformedURLException, IOException {
        Document document=null;
		  SAXReader saxReader = new SAXReader(false);
        saxReader.setEncoding("UTF-8");

		  URL url=new URL(Filename);
		  if (url.getProtocol().equalsIgnoreCase("file")) {
				document=saxReader.read(Filename);
		  } else {
				DefaultHttpClient client=HttpClientFactory.getClient();
				HttpGet get=new HttpGet(Filename);
				HttpResponse response=client.execute(get);
				document=saxReader.read(response.getEntity().getContent());
				get.abort();
		  }
		  
        return document;
    }

    public static Document readXML(Reader reader) throws DocumentException {
        SAXReader saxReader = new SAXReader(false);
        saxReader.setEncoding("UTF-8");
        return saxReader.read(reader);
    }

    public static Document readXML(InputStream inputStream) throws DocumentException {
        SAXReader saxReader = new SAXReader(false);
        saxReader.setEncoding("UTF-8");
        return saxReader.read(inputStream);
    }

    public static Document readXMLResource(String ResourceName) throws DocumentException {
        Document doc;

        SAXReader saxReader = new SAXReader(false);
        saxReader.setEncoding("UTF-8");
        doc = saxReader.read(System.out.getClass().getResourceAsStream(ResourceName));
        return doc;
    }

    public static Document parseHtml(String UrlStr) throws SAXException, DocumentException, IOException {
        String ParserClassname = "org.ccil.cowan.tagsoup.Parser";
        String NamespaceStr = "http://www.w3.org/1999/xhtml";
		  Document document=null;

        // use tagsoup-parser
        XMLReader parser = XMLReaderFactory.createXMLReader(ParserClassname);
        SAXReader saxReader = new SAXReader(parser, false);
        saxReader.setEncoding("UTF-8");

		  DefaultHttpClient client=HttpClientFactory.getClient();
		  HttpGet get=new HttpGet(UrlStr);

		  HttpResponse response=client.execute(get);
		  InputStream inStream=response.getEntity().getContent();
		  document=saxReader.read(inStream);
		  get.abort();

        return document;
    }

    public static Document parseHtml(InputStream inputStream) throws SAXException, DocumentException {
        String ParserClassname = "org.ccil.cowan.tagsoup.Parser";
        String NamespaceStr = "http://www.w3.org/1999/xhtml";

        // use tagsoup-parser
        XMLReader parser = XMLReaderFactory.createXMLReader(ParserClassname);
        SAXReader saxReader = new SAXReader(parser, false);
        saxReader.setEncoding("UTF-8");
        return saxReader.read(inputStream);
    }

    public static Document parseHtml(Reader reader) throws SAXException, DocumentException {
        String ParserClassname = "org.ccil.cowan.tagsoup.Parser";
        String NamespaceStr = "http://www.w3.org/1999/xhtml";

        // use tagsoup-parser
        XMLReader parser = XMLReaderFactory.createXMLReader(ParserClassname);
        SAXReader saxReader = new SAXReader(parser, false);
        saxReader.setEncoding("UTF-8");
        return saxReader.read(reader);
    }

    public static String readNumberFromHtml(InputStream inputStream, String xpathStr) throws SAXException, DocumentException, IOException {
        String value = null;

        Document document = parseHtml(inputStream);
         Logger.getLogger(XMLTools1.class.getName()).info(writeTempXMLFile(document));

        Node node = document.selectSingleNode(xpathStr);
        if (node != null) {
            value = node.getText();
            value = getNumberString(value);
        } else {
            Logger.getLogger(XMLTools1.class.getName()).warning(
                    "Could not extract " + xpathStr
                    + "\n check file: " + writeTempXMLFile(document));
        }
        return value;
    }

    public static String readValueFromHtml(InputStream inputStream, String xpathStr) throws SAXException, DocumentException, IOException {
        String value = null;

        Document document = parseHtml(inputStream);
        Node node = document.selectSingleNode(xpathStr);
        if (node != null) {
            value = node.getText();
        } else {
            File file = File.createTempFile("manticore", ".xml");
            writeToXML(document, file);
            Logger.getLogger(XMLTools1.class.getName()).warning(
                    "Could not extract " + xpathStr
                    + "\n check file: " + file.getAbsolutePath());
        }
        return value;
    }

    public static String readValueFromHtml(InputStream inputStream, String xpathStr, Pattern p) throws SAXException, DocumentException, IOException {
        String value = null;
        Document document = parseHtml(inputStream);
        Node node = document.selectSingleNode(xpathStr);
        if (node != null) {
            value = extractString(node.getText(),p);
        } else {
            writeXMLDocumentWarning(document, xpathStr);
        }
        return value;
    }

    public static void writeXMLDocumentWarning(Document document, String errorItem) {
        String msg=new StringBuffer()
                .append("Problem with ")
                .append(errorItem)
                .append("\n check file: ")
                .append(writeTempXMLFile(document))
                .toString();
        Logger.getAnonymousLogger().warning(msg);

    }

    public static int getIndexFromHtml(InputStream inputStream, String key, String xpathStr) throws SAXException, DocumentException {
        Document document = parseHtml(inputStream);
        ListIterator<Node> iterator = document.selectNodes(xpathStr).listIterator();
        int i = 0;

        while (iterator.hasNext() && (!iterator.next().getText().contains(key))) {
            i++;
        }
        return i;
    }

    public static void transformToHtml(Document doc, String xslfilename, String htmlfilename) {
        try {
            // load the transformer using JAXP
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(System.out.getClass().getResource(xslfilename).getFile()));

            // now lets style the given document
            DocumentResult result = new DocumentResult();
            transformer.transform(new DocumentSource(doc), result);

            // write to file
            writeToXML(result.getDocument(), htmlfilename);


        } catch (Exception x) {
            //Logger.getLogger(this.getClass().getName()).severe(x.getMessage());
        }
    }

    public static String getHomeDir() {
        String p = System.getProperty("user.home");
        String s = System.getProperty("file.separator");
        if (p.lastIndexOf(s) < p.length()) {
            p += s;
        }
        return p;
    }

    public static String getResourceUrlStr(String resourceName) {
        String urlStr = System.out.getClass().getResource(resourceName).toExternalForm();
        return urlStr;
    }

    public static final String extractString(String s, Pattern p) {
        String result = "";

        if (s.length() > 0) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                result = m.group(1);
            } else {
                //Logger.getLogger(XMLTools.class.getName()).warning("could not extract pattern " + p.pattern() + " from: " + s);
            }
        }
        return result;
    }

    public static final Float extractFloat(String s) throws ParseException {
        String result = "";
        result = getNumberString(s);
        return DecimalFormat.getInstance(Locale.GERMAN).parse(result).floatValue();
    }

    public static final Double extractDouble(String s) throws ParseException {
        String result = "";
        result = getNumberString(s);
        return DecimalFormat.getInstance(Locale.GERMAN).parse(result).doubleValue();
    }

    public static final Integer extractInteger(String s) throws ParseException {
        String result = "";
        result = getNumberString(s);
        return DecimalFormat.getInstance(Locale.GERMAN).parse(result).intValue();
    }

    public static boolean containsNumber(String s) {
        return s.matches("([0-9]*)");
    }

    public static String getNumberString(String s) {
        String result = "";
        if (s.length() > 0) {
            Matcher m = DECIMAL_NUMBER_PATTERN.matcher(s);
            if (m.find()) {
                result = m.group(1);
            } else {
                Logger.getLogger(XMLTools1.class.getName()).fine("could not extract number from: " + s);
            }
        }
        return result;
    }

    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
        String s="";
        if (inputStream != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }
            s = sb.toString();
        }
        return s;
    }
}

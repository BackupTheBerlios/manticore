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
package com.manticore.parser;

import com.manticore.http.HttpClientFactory;
import com.manticore.http.HttpXMLTools;
import com.manticore.util.XMLTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author are
 */
public class WebsiteParser {

    private static WebsiteParser instance = null;
    private HashMap<String, Site> siteHashMap;
    private final static Logger logger = Logger.getLogger(WebsiteParser.class.getName());
    public boolean writeTempXMLFiles=false;
    public boolean readInternalConfig=false;

    private WebsiteParser() {
        siteHashMap = new HashMap<String, Site>();
        try {
            Document document = readInternalConfig ? XMLTools.readXMLResource("/com/manticore/parser/parser.xml")
                                                    : XMLTools.readXML("http://www.manticore-projects.com/download/parser.xml");
            // read sites
            Iterator<Element> siteIterator = document.getRootElement().elementIterator("site");
            while (siteIterator.hasNext()) {
                Element element = siteIterator.next();
                Site site = new Site();
                site.id = element.attributeValue("id");
                site.urlStr = element.attributeValue("urlStr");
                site.xpath = element.attributeValue("xpath");

                site.parameterHashMap = new HashMap<String, Parameter>();
                Iterator<Element> parameterIterator = element.elementIterator("parameter");
                while (parameterIterator.hasNext()) {
                    Element parameterElement = parameterIterator.next();
                    Parameter parameter = new Parameter();
                    parameter.id = parameterElement.attributeValue("id");
                    parameter.key = parameterElement.attributeValue("key");
                    site.parameterHashMap.put(parameter.id, parameter);
                }

                site.fieldHashMap = new HashMap<String, Field>();
                Iterator<Element> fieldIterator = element.elementIterator("field");
                while (fieldIterator.hasNext()) {
                    Element fieldElement = fieldIterator.next();

                    Field field = new Field();
                    field.id = fieldElement.attributeValue("id");
                    field.description = fieldElement.elementText("description");
                    field.xpath = fieldElement.elementText("xpath");
                    field.pattern = fieldElement.elementText("pattern");
                    field.type = fieldElement.elementText("type");
                    field.locale_in = fieldElement.elementText("locale_in");
                    field.locale_out = fieldElement.elementText("locale_out");
                    field.asXML = fieldElement.elementText("asXML");

                    site.fieldHashMap.put(field.id, field);
                }

                siteHashMap.put(site.id, site);
            }

        } catch (MalformedURLException ex) {
				Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
		  } catch (IOException ex) {
				Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
		  } catch (DocumentException ex) {
            Logger.getLogger(WebsiteParser.class.getName()).log(Level.FINE, null, ex);
        }
    }

    public static WebsiteParser getInstance() {
        if (instance == null) {
            instance = new WebsiteParser();
        }
        return instance;
    }

    public Site getSite(String id, Reader reader) {
        Site site = null;
        if (siteHashMap.containsKey(id)) {
            site = siteHashMap.get(id);
            site.selectNodes(reader);
        }
        return site;
    }

    public Site getSite(String id, HashMap<String, String> parameterHashMap, DefaultHttpClient client) {
        Site site = null;
        if (siteHashMap.containsKey(id)) {
            site = siteHashMap.get(id);
            site.selectNodes(parameterHashMap, client);
        }
        return site;
    }

    public HttpResponse getResponseFromPost(String id, HashMap<String, String> parameterHashMap, DefaultHttpClient client, HttpEntity entity) {
        HttpResponse response = null;
        if (siteHashMap.containsKey(id)) {
            response = siteHashMap.get(id).getResponseFromPost(parameterHashMap, client, entity);
        }
        return response;
    }

    public String getHeaderValueFromPost(String id, HashMap<String, String> parameterHashMap, DefaultHttpClient client, HttpEntity entity, String headerName) {
        String headerValue="";
        if (siteHashMap.containsKey(id)) {
            headerValue = siteHashMap.get(id).getHeaderValueFromPost(parameterHashMap, client, entity, headerName);
        }
        return headerValue;
    }

    public String getHeaderValueFromPost(String id, String parameterId, String parameterValue, DefaultHttpClient client, HttpEntity entity, String headerName) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        return getHeaderValueFromPost(id, parameterList, client, entity, headerName);
    }

    public String getHeaderValueFromPost(String id, DefaultHttpClient client, HttpEntity entity, String headerName) {
        HashMap<String, String> parameterList = new HashMap();
        return getHeaderValueFromPost(id, parameterList, client, entity, headerName);
    }

    public String getHeaderValueFromPost(String id, String[][] parameterArray, DefaultHttpClient client, HttpEntity entity, String headerName) {
        HashMap<String, String> parameterList = new HashMap();
        for (int l = 0; l < parameterArray.length; l++) {
            parameterList.put(parameterArray[l][0], parameterArray[l][1]);
        }
        return getHeaderValueFromPost(id, parameterList, client, entity, headerName);
    }

    public Site getSiteFromPost(String id, HashMap<String, String> parameterHashMap, DefaultHttpClient client, HttpEntity entity) {
        Site site = null;
        if (siteHashMap.containsKey(id)) {
            site = siteHashMap.get(id);
            site.selectNodesFromPost(parameterHashMap, client, entity);
        }
        return site;
    }

    public Site getSiteFromPost(String id, String parameterId, String parameterValue, DefaultHttpClient client, HttpEntity entity) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        return getSiteFromPost(id, parameterList, client, entity);
    }

    public Site getSiteFromPost(String id, DefaultHttpClient client, HttpEntity entity) {
        HashMap<String, String> parameterList = new HashMap();
        return getSiteFromPost(id, parameterList, client, entity);
    }

    public Site getSiteFromPost(String id, String[][] parameterArray, DefaultHttpClient client, HttpEntity entity) {
        HashMap<String, String> parameterList = new HashMap();
        for (int l = 0; l < parameterArray.length; l++) {
            parameterList.put(parameterArray[l][0], parameterArray[l][1]);
        }
        return getSiteFromPost(id, parameterList, client, entity);
    }

    public Site getSite(String id, HashMap<String, String> parameterHashMap) {
        Site site = null;
        if (siteHashMap.containsKey(id)) {
            site = siteHashMap.get(id);
            site.selectNodes(parameterHashMap);
        }
        return site;
    }

    public Site getSite(String id) {
        HashMap<String, String> parameterList = new HashMap();
        return getSite(id, parameterList);
    }

    public Site getSite(String id, DefaultHttpClient client) {
        HashMap<String, String> parameterList = new HashMap();
        return getSite(id, parameterList, client);
    }

    public Site getSite(String id, String parameterId, String parameterValue, DefaultHttpClient client) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        return getSite(id, parameterList, client);
    }

    public Site getSite(String id, String parameterId, String parameterValue) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        return getSite(id, parameterList);
    }

    public Site getSite(String id, String[][] parameterArray, DefaultHttpClient client) {
        HashMap<String, String> parameterList = new HashMap();
        for (int l = 0; l < parameterArray.length; l++) {
            parameterList.put(parameterArray[l][0], parameterArray[l][1]);
        }
        return getSite(id, parameterList, client);
    }

    public Site getSite(String id, String[][] parameterArray) {
        HashMap<String, String> parameterList = new HashMap();
        for (int l = 0; l < parameterArray.length; l++) {
            parameterList.put(parameterArray[l][0], parameterArray[l][1]);
        }
        return getSite(id, parameterList);
    }

    public InputStream getSiteContentStream(String id, HashMap<String, String> parameterHashMap, DefaultHttpClient client) {
        InputStream inputStream = null;
        if (siteHashMap.containsKey(id)) {
            Site site = siteHashMap.get(id);
            inputStream = site.getContentStream(parameterHashMap, client);
        }
        return inputStream;
    }

    public InputStream getSiteContentStream(String id, HashMap<String, String> parameterHashMap) {
        return getSiteContentStream(id, parameterHashMap, HttpClientFactory.getClient());
    }

    public InputStream getSiteContentStream(String id, String parameterId, String parameterValue) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        return getSiteContentStream(id, parameterList, HttpClientFactory.getClient());
    }

    public String getSiteContentString(String id, String parameterId, String parameterValue,  DefaultHttpClient client) {
        HashMap<String, String> parameterList = new HashMap();
        parameterList.put(parameterId, parameterValue);
        InputStream inputStream = getSiteContentStream(id, parameterList, client);

        String s = "";
        if (inputStream != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } finally {
                    inputStream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            s = sb.toString();
        }
        return s;
    }

    public class Site {

        String id;
        String urlStr;
        String xpath;
        HashMap<String, Parameter> parameterHashMap;
        HashMap<String, Field> fieldHashMap;
        HashMap<String, String> fieldValueHashMap = new HashMap<String, String>();
        Iterator<Node> nodeIterator;

        public String getHeaderValue(HashMap<String, String> parameterList, DefaultHttpClient client, String headerName) {
            String headerValue="";
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                response = client.execute(new HttpGet(finalUrlStr));
                headerValue=response.getHeaders(headerName)[0].getValue();
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return headerValue;
        }

        public  String getHeaderValueFromPost(HashMap<String, String> parameterList, DefaultHttpClient client, HttpEntity entity, String headerName) {
            String headerValue="";
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                HttpPost post = new HttpPost(finalUrlStr);
                post.setEntity(entity);
                response = client.execute(post);
                headerValue=response.getHeaders(headerName)[0].getValue();
                post.abort();
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return headerValue;
        }

        public HttpResponse getResponse(HashMap<String, String> parameterList, DefaultHttpClient client) {
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                response = client.execute(new HttpGet(finalUrlStr));
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return response;
        }

        public  HttpResponse getResponseFromPost(HashMap<String, String> parameterList, DefaultHttpClient client, HttpEntity entity) {
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                HttpPost post = new HttpPost(finalUrlStr);
                post.setEntity(entity);
                response = client.execute(post);
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return response;
        }

        public InputStream getContentStream(HashMap<String, String> parameterList, DefaultHttpClient client) {
            InputStream inputStream = null;
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                response = client.execute(new HttpGet(finalUrlStr));
                inputStream = response.getEntity().getContent();
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return inputStream;
        }

        public void selectNodesFromPost(HashMap<String, String> parameterList, DefaultHttpClient client, HttpEntity entity) {
            Document document = null;
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                HttpPost post = new HttpPost(finalUrlStr);
                post.setEntity(entity);
                response = client.execute(post);

                try {
                    document = XMLTools.parseHtml(response.getEntity().getContent());

                    if (writeTempXMLFiles) logger.info(XMLTools.writeTempXMLFile(document));
                    
                    if (xpath.length() == 0) {
                        Vector<Node> nodeList = new Vector<Node>();
                        nodeList.add(document);
                        nodeIterator = nodeList.iterator();
                    } else {
                        nodeIterator = document.selectNodes(xpath).iterator();
                    }
                } catch (SAXException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    logger.log(Level.INFO, "could not parse {0}", finalUrlStr);
                } catch (DocumentException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    logger.log(Level.INFO, "could not read {0}", finalUrlStr);
                }

                post.abort();
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void selectNodes(HashMap<String, String> parameterList, DefaultHttpClient client) {
            Document document = null;
            HttpResponse response = null;

            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                response = client.execute(new HttpGet(finalUrlStr));

                try {
                    document = XMLTools.parseHtml(response.getEntity().getContent());

                    if (writeTempXMLFiles) logger.info(XMLTools.writeTempXMLFile(document));

                    if (xpath.length() == 0) {
                        Vector<Node> nodeList = new Vector<Node>();
                        nodeList.add(document);
                        nodeIterator = nodeList.iterator();
                    } else {
                        nodeIterator = document.selectNodes(xpath).iterator();
                    }
                } catch (SAXException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    logger.log(Level.INFO, "could not parse {0}", finalUrlStr);
                } catch (DocumentException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    logger.log(Level.INFO, "could not read {0}", finalUrlStr);
                }
            } catch (IOException ex) {
                Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        public void selectNodes(HashMap<String, String> parameterList) {
            Document document = null;
            String finalUrlStr = getFinalUrlStr(parameterList);
            try {
                document = HttpXMLTools.parseHtml(finalUrlStr);

                if (writeTempXMLFiles) logger.info(XMLTools.writeTempXMLFile(document));

                if (xpath.length() == 0) {
                    Vector<Node> nodeList = new Vector<Node>();
                    nodeList.add(document);
                    nodeIterator = nodeList.iterator();
                } else {
                    nodeIterator = document.selectNodes(xpath).iterator();
                }
            } catch (IOException ex) {
					logger.log(Level.SEVERE, null, ex);
				} catch (SAXException ex) {
                logger.log(Level.SEVERE, null, ex);
                logger.info("could not parse " + finalUrlStr);
            } catch (DocumentException ex) {
                logger.log(Level.SEVERE, null, ex);
                logger.info("could not read " + finalUrlStr);
            }
        }

        public int getRowIndex(String id, String key) {
            int i = 0;
            while (hasNextNode() && (!getString(id).contains(key))) {
                i++;
            }
            return i;
        }

        public boolean hasNextNode() {
            boolean hasNext = nodeIterator.hasNext();
            if (hasNext) {
                Node rootNode = nodeIterator.next();
                Iterator<Field> fieldIterator = fieldHashMap.values().iterator();
                while (fieldIterator.hasNext()) {
                    Field field = fieldIterator.next();
                    Node node = rootNode.selectSingleNode(field.xpath);
                    if (node != null) {
                        String value = field.type.equalsIgnoreCase("XML") || field.asXML.equals("1") ? node.asXML() : node.getText();
                        if (value.length() > 0 && field.pattern.length() > 0) {
                            Pattern p = Pattern.compile(field.pattern, Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.DOTALL);
                            Matcher m = p.matcher(value);

									 StringBuilder stringBuilder=new StringBuilder();
                            while (m.find()) stringBuilder.append(" ").append( m.group(1) );
                            
									 String result=stringBuilder.toString();
									 if (result.length()==0) logger.fine("could extract " + field.pattern + " from " + value);
                            value=result.trim();
                        }

                        if (field.type.length() > 0 && value.length()>0) {
                            Locale locale = Locale.getDefault();

                            if (field.locale_in.equalsIgnoreCase("de")) {
                                locale = Locale.GERMAN;
                            } else if (field.locale_in.equalsIgnoreCase("de_DE")) {
                                locale = Locale.GERMANY;
                            } else if (field.locale_in.equalsIgnoreCase("en")) {
                                locale = Locale.ENGLISH;
                            } else if (field.locale_in.equalsIgnoreCase("en_GB")) {
                                locale = Locale.UK;
                            } else if (field.locale_in.equalsIgnoreCase("en_US")) {
                                locale = Locale.US;
                            }

                            if (field.type.equalsIgnoreCase("INTEGER")) {
                                try {
                                    Number number = DecimalFormat.getIntegerInstance(locale).parse(value);
                                    value = String.valueOf(number.intValue());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else if (field.type.equalsIgnoreCase("FLOAT")) {
                                try {
                                    Number number = DecimalFormat.getInstance(locale).parse(value);
                                    value = String.valueOf(number.doubleValue());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else if (field.type.equalsIgnoreCase("PERCENT")) {
                                try {
                                    Number number = DecimalFormat.getPercentInstance(locale).parse(value);
                                    value = String.valueOf(number.doubleValue());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } else if (field.type.equalsIgnoreCase("FRACTION")) {
                                String valueArr[] = value.split(":");
                                if (valueArr.length > 1) {
                                    double numerator;
                                    try {
                                        numerator = DecimalFormat.getInstance(locale).parse(valueArr[0]).doubleValue();
                                        double denominator = DecimalFormat.getInstance(locale).parse(valueArr[1]).doubleValue();
                                        double ratio = numerator / denominator;
                                        value = String.valueOf(ratio);
                                    } catch (ParseException ex) {
                                        Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            } else if (field.type.equalsIgnoreCase("DATE")) {
                                try {
                                    Date date = DateFormat.getDateInstance(DateFormat.MEDIUM, locale).parse(value);
                                    value = String.valueOf(date.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else if (field.type.equalsIgnoreCase("TIME")) {
                                try {
                                    Date date = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale).parse(value);
                                    value = String.valueOf(date.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else if (field.type.equalsIgnoreCase("DATETIME")) {
                                try {
                                    Date date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale).parse(value);
                                    value = String.valueOf(date.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(WebsiteParser.class.getName()).log(Level.SEVERE, null, ex);
                                }

                            } 
                        }
                        fieldValueHashMap.put(field.id, value);
                    } else {
                        logger.fine(new StringBuffer().append("could not read ").append(field.xpath).append("\nplease check ").append(XMLTools.writeTempXMLFile(rootNode.getDocument())).toString());
                    }
                }
            }
            return hasNext;
        }

        private String getFinalUrlStr(HashMap<String, String> parameterList) {
            StringBuilder stringBuffer = new StringBuilder(urlStr);
            Iterator<Entry<String, String>> iterator = parameterList.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                if (parameterHashMap.containsKey(entry.getKey())) {
                    Parameter parameter = parameterHashMap.get(entry.getKey());
                    stringBuffer.append(parameter.key);
                    stringBuffer.append(entry.getValue());
                }
            }
            return stringBuffer.toString();
        }

        public String getString(String id) {
            String value = "";
            if (fieldValueHashMap.containsKey(id)) {
                value = fieldValueHashMap.get(id);
            }
            return value;
        }

        public float getFloat(String id) {
            float value = 0f;
            if (fieldValueHashMap.containsKey(id)) {
                if (fieldValueHashMap.get(id).length()>0) value = Float.valueOf(fieldValueHashMap.get(id));
            }
            return value;
        }

        public double getDouble(String id) {
            double value = 0d;
            if (fieldValueHashMap.containsKey(id)) {
                if (fieldValueHashMap.get(id).length()>0) value = Double.valueOf(fieldValueHashMap.get(id));
            }
            return value;
        }

        public int getInt(String id) {
            int value = 0;
            if (fieldValueHashMap.containsKey(id)) {
                if (fieldValueHashMap.get(id).length()>0) value = Integer.valueOf(fieldValueHashMap.get(id));
            }
            return value;
        }

        public long getLong(String id) {
            long value = 0l;
            if (fieldValueHashMap.containsKey(id)) {
                if (fieldValueHashMap.get(id).length()>0) value = Long.valueOf(fieldValueHashMap.get(id));
            }
            return value;
        }

        public Date getDateTime(String id) {
            Date value = null;
            if (fieldValueHashMap.containsKey(id)) {
                if (fieldValueHashMap.get(id).length()>0) value = new Date(Long.valueOf(fieldValueHashMap.get(id)));
            }
            return value;
        }

        private void selectNodes(Reader reader) {
            Document document=null;
            try {
                document = XMLTools.readXML(reader);

                if (writeTempXMLFiles) logger.info(XMLTools.writeTempXMLFile(document));
                
                if (xpath.length() == 0) {
                    Vector<Node> nodeList = new Vector<Node>();
                    nodeList.add(document);
                    nodeIterator = nodeList.iterator();
                } else {
                    nodeIterator = document.selectNodes(xpath).iterator();
                }
            } catch (DocumentException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    private class Parameter {

        String id;
        String key;
    }

    private class Field {

        String id;
        String description;
        String xpath;
        String pattern;
        String type;
        String locale_in;
        String locale_out;
        String asXML;
    }
}

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
package com.manticore.stream;

import com.manticore.foundation.Tick;
import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import com.manticore.util.XMLTools;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class ArivaQuoteStream extends Thread {

    public final static int TYPE_INDEX = 0;
    public final static int TYPE_DERIVATE_DB = 1;
    private javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
    static final Pattern pBid = Pattern.compile("b([\\d\\.,]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern pAsk = Pattern.compile("a([\\d\\.,]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern pCurrent = Pattern.compile("p([\\d\\.,]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern tCurrent = Pattern.compile("t([\\d]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    String urlStr;
    String refererStr;
    private HttpClient httpclient;
    private HttpGet httpget;
    private Float lastPrice;
    private Float currentPrice;
    private DateTime lastDateTime;
    private DateTime currentDateTime;
    private InputStream instream;
    private int type;
    private String key;
    private BufferedReader reader;
    private boolean stopped = false;
    private final static Logger logger = Logger.getLogger(ArivaQuoteStream.class.getName());
	 private Tick lastTick;

    public ArivaQuoteStream(String key) {
        this.type = TYPE_INDEX;
        this.key = key;

        start();
    }

    public ArivaQuoteStream(String wkn, String exchange_key) {
        this.type = TYPE_DERIVATE_DB;
        this.key = wkn;

        start();
    }

    public static String getArivaSecuIDFromWKN(String wkn) {
        String secu = "";
        Site site = WebsiteParser.getInstance().getSite("ArivaIDByWKN", "wkn", wkn);
        if (site.hasNextNode()) {
            secu = site.getString("secu");
        }
        return secu;
    }

    public void run() {
        if (type == TYPE_DERIVATE_DB) {
            key = getArivaSecuIDFromWKN(key) + "@31.6";
        }

        instream = WebsiteParser.getInstance().getSiteContentStream("ArivaPushStream", "secu", key);
        if (instream != null) {
            try {
                try {
                    reader = new BufferedReader(new InputStreamReader(instream));
                    char[] b = new char[32];
                    int i = 0;

                    boolean doAppend = false;
                    StringBuffer stringBuffer = new StringBuffer();

                    while (!stopped) {
                        try {
                            if (reader.ready() && reader.read(b) > 0) {
                                i++;
                                for (int k = 0; k < b.length; k++) {
                                    if (i > 2 && b[k] == '(' & !doAppend) {
                                        stringBuffer = new StringBuffer();
                                        doAppend = true;
                                    } else if (b[k] == ')' && doAppend) {
                                        String s = stringBuffer.toString();
                                        handleHeader(s);
                                        doAppend = false;
                                    } else if (doAppend) {
                                        stringBuffer.append(b[k]);
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                    reader.close();
                    instream.close();
                } catch (RuntimeException ex) {
                } finally {

                    // Closing the input stream will trigger connection release
                    // System.out.println("bye bye");
                    instream.close();
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        //WatchDog.getInstance().unregister(this);
        Logger.getLogger(this.getClass().getName()).fine("stream closed");
    }

    public void stopThread() {
        //System.out.println("try to close stream now");
        stopped = true;
        //httpget.abort();
        interrupt();
        //System.out.println("should be closed now!");
    }

    // This methods allows classes to register for MyEvents
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }
    // This methods allows classes to unregister for MyEvents

    public void removeChangeListener(ChangeListener listener) {
        try {
            listenerList.remove(ChangeListener.class, listener);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).severe(ex.getMessage());
        }
    }
    // This private class is used to fire MyEvents

    void fireMyEvent(ChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(evt);
            }
        }
    }

    public DateTime getCurrentDateTime() {
        return currentDateTime;
    }

    public DateTime getLastDateTime() {
        return lastDateTime;
    }

    public Float getCurrentPrice() {
        return currentPrice;
    }

    public Float getLastPrice() {
        return lastPrice;
    }

    private void handleHeader(String s) {
        try {
            lastPrice = currentPrice;
            if (type == TYPE_DERIVATE_DB) {
                currentPrice = DecimalFormat.getInstance(Locale.UK).parse(XMLTools.extractString(s, pAsk)).floatValue();
            } else if (type == TYPE_INDEX) {
                currentPrice = DecimalFormat.getInstance(Locale.UK).parse(XMLTools.extractString(s, pCurrent)).floatValue();

                if (s.matches("t([\\d]+)")) {
                    lastDateTime = currentDateTime;
                    currentDateTime = DateTimeFormat.forPattern("yyyyMMddHHmmss").parseDateTime(XMLTools.extractString(s, tCurrent));
                    //System.out.println("found time " + currentDateTime.toString());
                } else {
                    lastDateTime = currentDateTime;
                    //currentDateTime =new DateTime(DateTimeZone.forID("Europe/Berlin"));
                    currentDateTime = new DateTime();
                    //System.out.println("set time: " + currentDateTime.toString());
                }

            }
				
				lastTick=new Tick(currentDateTime, currentPrice, 0L);

            Logger.getLogger(this.getClass().getName()).fine(s + " --> " + currentPrice + "(" + currentDateTime + ")");

            ChangeEvent changeEvent = new ChangeEvent(this);
            fireMyEvent(changeEvent);
        } catch (ParseException ex) {
            Logger.getLogger(this.getClass().getName()).finest("Error parsing " + s);
        }
    }

    /**
     * @return the listenerList
     */
    public javax.swing.event.EventListenerList getListenerList() {
        return listenerList;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    public static void main(String[] args) {
        System.out.println(getArivaSecuIDFromWKN("DE000DB8JES1"));
        ArivaQuoteStream stream = new ArivaQuoteStream("DE000DB8JES1", "@31.6");
        System.out.println(stream.currentPrice);
    }

	 /**
	  * @return the lastTick
	  */
	 public Tick getLastTick() {
		  return lastTick;
	 }
}

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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author are
 */
public class WatchDog extends TimerTask implements ChangeListener {
    public final static long PERIOD=120000L;

    private HashMap<ArivaQuoteStream,Date> streamHashMap=new HashMap();
    private static WatchDog instance=null;

    private WatchDog() {
        Logger.getLogger(this.getClass().getName()).fine("start WatchDog for broken streams");

        Timer timer=new Timer(this.getClass().getName(), true);
        timer.schedule(this, PERIOD, PERIOD);
    }

    public static WatchDog getInstance() {
        if (instance==null) {
            instance=new WatchDog();
        }
        return instance;
    }

    public void register(ArivaQuoteStream arivaStreamQuote) {
        Date date=new Date();
        streamHashMap.put(arivaStreamQuote, date);
    }

    public void unregister(ArivaQuoteStream arivaStreamQuote) {
        streamHashMap.remove(arivaStreamQuote);
        arivaStreamQuote.stopThread();
    }

    @Override
    public void run() {
        Logger.getLogger(this.getClass().getName()).fine("search for broken streams");

        Date date=new Date();

        Iterator<ArivaQuoteStream> streamIterator=streamHashMap.keySet().iterator();


        while (streamIterator.hasNext()) {
            ArivaQuoteStream streamQuote=streamIterator.next();
            long millis=date.getTime()-streamHashMap.get(streamQuote).getTime();

            if (millis>PERIOD && streamQuote.isAlive() &! streamQuote.isInterrupted()) {
                Logger.getLogger(this.getClass().getName()).warning("found dead stream, restart it");
                restartStream(streamQuote);
            }
        }

    }

    private void restartStream(ArivaQuoteStream brokenStream) {
        String key=brokenStream.getKey();
        int type=brokenStream.getType();
        Object[] listeners = brokenStream.getListenerList().getListenerList();
        
        //@todo: separate different types here!!!
        // you broke this temporarly
        ArivaQuoteStream newStream= new ArivaQuoteStream(key);

        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ChangeListener changeListener=((ChangeListener) listeners[i + 1]);
                brokenStream.removeChangeListener(changeListener);
                newStream.addChangeListener( changeListener );
            }
        }

        brokenStream.stopThread();
    }

    public void restartAllStreams() {
        Logger.getLogger(this.getClass().getName()).info("restart all streams upon request");
        ArivaQuoteStream[] streamArr=streamHashMap.keySet().toArray(new ArivaQuoteStream[0]);
        
        for (int i=0; i<streamArr.length; i++) {
            ArivaQuoteStream streamQuote=streamArr[i];
            restartStream(streamQuote);

        }
    }

    public void stateChanged(ChangeEvent e) {
        ArivaQuoteStream arivaStreamQuote=(ArivaQuoteStream) e.getSource();
    }

    public void removeChangeListener(ChangeListener changeListener) {
        ArivaQuoteStream[] streamArr=streamHashMap.keySet().toArray(new ArivaQuoteStream[0]);

        for (int k=0; k<streamArr.length; k++) {
            ArivaQuoteStream streamQuote=streamArr[k];

            Object[] listeners = streamQuote.getListenerList().getListenerList();

            // Each listener occupies two elements - the first is the listener class
            // and the second is the listener instance
            for (int i = 0; i < listeners.length; i += 2) {
                if (listeners[i] == ChangeListener.class && changeListener.equals( (ChangeListener) listeners[i + 1] )) {
                    streamQuote.removeChangeListener(changeListener);
                }
            }

            //streamQuote.interrupt();
        }
    }
}

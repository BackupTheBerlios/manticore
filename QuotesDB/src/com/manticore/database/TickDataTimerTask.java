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
package com.manticore.database;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author are
 */
public class TickDataTimerTask extends TimerTask {

    public final static long TIMER_PERIOD = 180000L;
    private long id_instrument;
    private long id_stockExchange;
    private Timer datasourceTimer=null;
    protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();

    public TickDataTimerTask(long id_instrument, long id_stockExchange, Date firstTime) {
        this.id_instrument = id_instrument;
        this.id_stockExchange = id_stockExchange;

        //if (datasourceTimer!=null) datasourceTimer.

        datasourceTimer = new Timer(true);
        datasourceTimer.schedule(this, 0, TIMER_PERIOD);
    }

    public void run() {
       boolean newDataFound=Quotes.getInstance().importInstrumentTickData(id_instrument, id_stockExchange);
       if (newDataFound) {
           fireMyEvent(new ChangeEvent(this));
       }
    }

    // This methods allows classes to register for MyEvents
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }
    // This methods allows classes to unregister for MyEvents

    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    // This private class is used to fire MyEvents
    void fireMyEvent(ChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == ChangeListener.class) {
                ((ChangeListener) listeners[i + 1]).stateChanged(evt);
            }
        }
    }
}

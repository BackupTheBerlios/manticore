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

import com.manticore.foundation.Tick;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author are
 */
public class FakeTickDataTimerTask extends Thread {
	 private final static Logger logger=Logger.getLogger(FakeTickDataTimerTask.class.getName());
	 private final static long SLEEP_PERIOD=10L;
    private long id_instrument;
    private long id_stockExchange;
	 private Date dateFrom;
	 private Date dateTo;

	 protected javax.swing.event.EventListenerList listenerList = new javax.swing.event.EventListenerList();
	 private Tick tick;

    public FakeTickDataTimerTask(long id_instrument, long id_stockExchange, Date dateFrom, Date dateTo) {
        this.id_instrument = id_instrument;
        this.id_stockExchange = id_stockExchange;
		  this.dateFrom=dateFrom;
		  this.dateTo=dateTo;
    }

	 @Override
    public void run() {
       try {
            final ResultSet rs = Quotes.getInstance().getTickdataResultSet(id_instrument, id_stockExchange, dateFrom, dateTo);
            while (rs.next()) {
					 tick=new Tick(rs.getTimestamp("timestamp"), rs.getFloat("price"), rs.getLong("quantity"));
					 fireMyEvent(new ChangeEvent(this));

					 try {
						  sleep(SLEEP_PERIOD);
					 } catch (InterruptedException ex) {
						  logger.log(Level.SEVERE, null, ex);
					 }
				}
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

	 public Tick getTick() {
		  return tick;
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

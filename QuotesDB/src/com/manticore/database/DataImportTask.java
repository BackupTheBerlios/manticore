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

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.MutableDateTime;

public class DataImportTask extends TimerTask {
    final static long period=900000L;
    Timer timer;

    public DataImportTask() {
        timer=new Timer(true);

        MutableDateTime mutableDateTime=new MutableDateTime(DateTimeZone.forID("Europe/Berlin"));
        mutableDateTime.setHourOfDay(8);
        mutableDateTime.setMinuteOfHour(0);
        mutableDateTime.setSecondOfMinute(0);
        mutableDateTime.setMillisOfSecond(0);

        Logger.getLogger(DataImportTask.class.getName()).info("start dataimport task...");

        timer.scheduleAtFixedRate(this, mutableDateTime.toDate() , period);
    }

    @Override
    public void run() {
        MutableDateTime mutableDateTime=new MutableDateTime(DateTimeZone.forID("Europe/Berlin"));
        int hour=mutableDateTime.getHourOfDay();
        int day=mutableDateTime.getDayOfWeek();
        
        if (hour>=8 && hour<24 && !(day==Calendar.SATURDAY && day==Calendar.SUNDAY)) {
            Quotes.logIn("org.postgresql.Driver", "jdbc:postgresql:wavetrader", System.getProperty("user.name"), "").importTickdata();
        }
    }

    
}

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

package com.manticore.foundation;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTimeConstants;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;

public class StockExchange {
    private long id;
    String symbol;
    String name;
    private int openingMinute = 9 * 60;
    private int closingMinute = 17 * 60 + 30;
    ArrayList<Date> holidayArrayList;
    ArrayList<Interval> excludedIntervalArrayList;

    public StockExchange() {
        name = "Lang und Schwarz";
        symbol="L&S";
        openingMinute = 8 * 60 + 30;
        closingMinute = 21 * 60 + 30;

        try {
            readHolidayArrayList();
        } catch (ParseException ex) {
            Logger.getLogger(StockExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public StockExchange(long id, String symbol, String description, int openingMinute, int closingMinute, ArrayList<Interval> excludedIntervalArrayList) {
        this.id=id;
        this.symbol=symbol;
        this.name=description;
        this.openingMinute=openingMinute;
        this.closingMinute=closingMinute;
        this.excludedIntervalArrayList=excludedIntervalArrayList;
    }

    private void readHolidayArrayList() throws ParseException {

        DateFormat df = DateFormat.getDateInstance();

        holidayArrayList = new ArrayList();
        holidayArrayList.add(df.parse("01.01.2008"));
        holidayArrayList.add(df.parse("02.01.2008"));
        holidayArrayList.add(df.parse("01.05.2008"));
        holidayArrayList.add(df.parse("03.10.2008"));
        holidayArrayList.add(df.parse("24.12.2008"));
        holidayArrayList.add(df.parse("25.12.2008"));
        holidayArrayList.add(df.parse("26.12.2008"));
        holidayArrayList.add(df.parse("31.12.2008"));
    }

    public boolean isBusinessDay(MutableDateTime dt) {
        return !isHoliday(dt);
    }
    
    public boolean isHoliday(MutableDateTime dt) {
        boolean b = false;
        
        b |= (dt.getDayOfWeek() == DateTimeConstants.SATURDAY);
        b |= (dt.getDayOfWeek() == DateTimeConstants.SUNDAY);
        b |= isInList( dt );

        return b;
    }

    public boolean isInList(MutableDateTime dt) {
        boolean result=false;

        Iterator<Interval> intervalIterator=excludedIntervalArrayList.iterator();
        while (intervalIterator.hasNext() &! result) {
            result |= (intervalIterator.next().contains(dt));
        }
        return result;
    }

    public String getSymbol() {
        return symbol;
    }
    
    public String getName() {
        return name;
    }

    public int getOpeningMinute() {
        return openingMinute;
    }
    
    public Date getOpeningDate() {
        MutableDateTime mutableDateTime=new MutableDateTime();
        mutableDateTime.setMinuteOfDay(openingMinute);
        mutableDateTime.setSecondOfMinute(0);
        mutableDateTime.setMillisOfSecond(0);
        
        return mutableDateTime.toDate();
    }

    public int getClosingMinute() {
        return closingMinute;
    }
    
    public Date getClosingDate() {
        MutableDateTime mutableDateTime=new MutableDateTime();
        mutableDateTime.setMinuteOfDay(closingMinute);
        mutableDateTime.setSecondOfMinute(0);
        mutableDateTime.setMillisOfSecond(0);
        
        return mutableDateTime.toDate();
    }
    
    public String toString() {
        return symbol + " " + name;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @param openingMinute the openingMinute to set
     */
    public void setOpeningMinute(int openingMinute) {
        this.openingMinute = openingMinute;
    }

    /**
     * @param closingMinute the closingMinute to set
     */
    public void setClosingMinute(int closingMinute) {
        this.closingMinute = closingMinute;
    }
}

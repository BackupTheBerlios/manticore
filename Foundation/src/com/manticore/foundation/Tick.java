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

import com.sun.org.apache.xerces.internal.impl.dv.xs.DecimalDV;
import java.text.DecimalFormat;
import java.util.Date;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Tick {

    Long count;
    private Float price;
    private Long quantity = Long.valueOf(0);
    private DateTime dateTime;
    boolean valid = false;

    /**
     *  Constructor for the Tick object
     *
     * @param  countstr  Description of Parameter
     * @param  datestr   Description of Parameter
     * @param  pricestr  Description of Parameter
     */
    public Tick(String countstr, String datestr, String pricestr) {
        try {
            count = Long.parseLong(countstr);
            MutableDateTime mdt = new MutableDateTime();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss");

            fmt.parseInto(mdt, datestr, 0);
            dateTime = mdt.toDateTime();
            price = Float.parseFloat(pricestr);
        } catch (Exception x) {
            System.out.println(x.getMessage());
        }
    }

    Tick(Long count, DateTime datetime, Float price) {
        this.count = count;
        this.dateTime = datetime;
        this.price = price;
    }

    public Tick(DateTime datetime, float price, boolean valid) {
        this.dateTime = datetime;
        this.price = Float.valueOf(price);
        this.valid = valid;
    }

    public Tick(Date date, float price, long quantity) {
        this.dateTime = new DateTime(date);
        this.price = price;
        this.quantity = quantity;
    }

    public Tick(DateTime date, float price, long quantity) {
        this.dateTime = date;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * @return the price
     */
    public Float getPrice() {
        return price;
    }

    /**
     * @param price the price to set
     */
    public void setPrice(Float price) {
        this.price = price;
    }

    /**
     * @return the quantity
     */
    public Long getQuantity() {
        return quantity;
    }

    /**
     * @return the dateTime
     */
    public DateTime getDateTime() {
        return dateTime;
    }

	 public Date getDate() {
		  return dateTime.toDate();
	 }

	 @Override
	 public String toString() {
		  String s=new StringBuilder()
					 .append(DateTimeFormat.shortDateTime().print(dateTime))
					 .append(" ")
					 .append(DecimalFormat.getNumberInstance().format(price))
					 .toString();
		  return s;
	 }
}


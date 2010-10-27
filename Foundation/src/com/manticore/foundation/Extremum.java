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

import org.joda.time.DateTime;
public class Extremum implements Comparable<Extremum> {
    public final static int TYPE_EXTREMUM_LOW=-2;
    public final static int TYPE_EXTREMUM_TMP_LOW=-1;
    public final static int TYPE_EXTREMUM_NONE=0;
    public final static int TYPE_EXTREMUM_TMP_HIGH=1;
    public final static int TYPE_EXTREMUM_HIGH=2;

    private DateTime dateTime;
    private Float price;
    private int type;

    public Extremum(DateTime dateTime, Float price, int type) {
        this.dateTime=dateTime;
        this.price=price;
        this.type=type;
    }

    public int compareTo(Extremum o) {
        return dateTime.compareTo(o.dateTime);
    }

    /**
     * @return the dateTime
     */
    public DateTime getDateTime() {
        return dateTime;
    }

    /**
     * @return the price
     */
    public Float getPrice() {
        return price;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
}

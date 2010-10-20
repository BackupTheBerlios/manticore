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
package com.manticore.report;

import java.util.Date;
import org.joda.time.DateTime;

public class TimeDataPoint extends DataPoint implements Comparable<TimeDataPoint> {
    Date x;

    public TimeDataPoint(String key, Float value) {
    }

    public TimeDataPoint(Date date, Float value) {
        this.x=date;
        this.value=value;
    }

    public Date getDate() {
        return x;
    }

    public void setDate(Date date) {
         this.x=date;
    }

    public DateTime getDateTime() {
        return new DateTime(x);
    }

    public String getCategory() {
        return null;
    }

    public int compareTo(TimeDataPoint o) {
        return x.compareTo(o.x);
    }

    @Override
    public Integer getSlot() {
        return null;
    }
}

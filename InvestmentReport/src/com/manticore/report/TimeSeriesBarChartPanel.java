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

import org.joda.time.DurationFieldType;

public class TimeSeriesBarChartPanel extends  TimeSeriesChartPanel {
    public final static DurationFieldType durationFieldType=DurationFieldType.days();

    @Override
    public TimeDataPoint getDataPoint(DataPoint dataPoint1, DataPoint dataPoint2) {
        TimeDataPoint dataPoint;

        if (dataPoint1 == null) {
            dataPoint = new TimeDataPoint(dataPoint2.getDate(), dataPoint2.value);
        } else {
            dataPoint = new TimeDataPoint(dataPoint1.getDate(), dataPoint1.value + dataPoint2.value);
        }
        return dataPoint;
    }


}

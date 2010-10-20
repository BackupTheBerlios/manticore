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

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;

public class TimeSeriesChartPanel extends ChartPanel {
    
    public static final DurationFieldType durationFieldType = DurationFieldType.days();

    private int getWeeks(DateTime d1, DateTime d2) {
        return d2.get(DateTimeFieldType.weekOfWeekyear())-d1.get(DateTimeFieldType.weekOfWeekyear()) +1;
    }

    private int getDays(DateTime d1, DateTime d2) {
        MutableDateTime mutableDateTime=d1.toMutableDateTime();
        int d=0;
        while (mutableDateTime.isBefore(d2)) {
            mutableDateTime.add(DurationFieldType.days(), 1);
            d++;
        }

        //long d1=d1.get(DateTimeFieldType.dayOfYear();
        //long d2=d2.get(DateTimeFieldType.)
        //return d2.get(DateTimeFieldType.dayOfYear())-d1.get(DateTimeFieldType.dayOfYear()) +1;
        return d;
    }

    @Override
    public ChartObject getChartObject() {
        ChartObject chartObject = new ChartObject();
        Float maxLineValue = null;
        Float minLineValue = null;
        Period period = new Period(dataPointVector.first().getDateTime(), dataPointVector.last().getDateTime());
        chartObject.gridLinesCount = getWeeks(dataPointVector.first().getDateTime(), dataPointVector.last().getDateTime());//period.toStandardWeeks().getWeeks() + 1;
        MutableDateTime mutableDateTime = new MutableDateTime(dataPointVector.first().getDate());
        mutableDateTime.setSecondOfDay(24 * 60 * 60 - 1);
        DataPoint[] dataPoints = dataPointVector.toArray(new TimeDataPoint[0]);
        chartObject.dataPoints = new TimeDataPoint[getDays(dataPointVector.first().getDateTime(), dataPointVector.last().getDateTime())];
        int k = 0;
        for (int i = 0; i < chartObject.dataPoints.length; i++) {
            mutableDateTime.add(TimeSeriesLineChartPanel.durationFieldType, 1);
            chartObject.dataPoints[i] = null;
            while (k < dataPoints.length && mutableDateTime.isAfter(dataPoints[k].getDate().getTime())) {
                chartObject.dataPoints[i] = getDataPoint(chartObject.dataPoints[i], dataPoints[k]);
                k++;
            }
            if (chartObject.dataPoints[i] != null) {
                if (maxLineValue == null || maxLineValue < chartObject.dataPoints[i].value) {
                    maxLineValue = chartObject.dataPoints[i].value;
                }
                if (minLineValue == null || minLineValue > chartObject.dataPoints[i].value) {
                    minLineValue = chartObject.dataPoints[i].value;
                }

                chartObject.average+=chartObject.dataPoints[i].value;
            }
        }
        chartObject.average/=chartObject.dataPoints.length;
        chartObject.setRange(maxLineValue, minLineValue);
        return chartObject;
    }

    public TimeDataPoint getDataPoint(DataPoint dataPoint1, DataPoint dataPoint2) {
        return new TimeDataPoint(dataPoint2.getDate(), dataPoint2.value);
    };
}

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

package com.manticore.chart;

import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class PeriodSettings {
    private DurationFieldType reportDurationFieldType;
    private int reportDurationFieldValue;
    private MutablePeriod reportPeriod;
    
    private DurationFieldType intervalDurationFieldType;
    private MutablePeriod intervalPeriod;
    
    private DurationFieldType candleDurationFieldType;
    private MutablePeriod candlePeriod;
    
    private DurationFieldType reportMarkerDurationFieldType;
    private int reportMarkerDurationFieldValue;
    private MutablePeriod reportMarkerPeriod;
    
    public PeriodSettings(
                        DurationFieldType reportDurationFieldType
                        , int reportPeriodValue
                        , DurationFieldType intervalDurationFieldType
                        , int intervalPeriodValue
                        , DurationFieldType reportMarkerDurationFieldType
                        , int reportMarkerPeriodValue
                        , DurationFieldType candleDurationFieldType
                        , int candlePeriodValue
                        ) {
        
        
        this.candleDurationFieldType=candleDurationFieldType;
        candlePeriod=getMutablePeriod(candleDurationFieldType,candlePeriodValue);
        
        this.intervalDurationFieldType=intervalDurationFieldType;
        intervalPeriod=getMutablePeriod(intervalDurationFieldType,intervalPeriodValue);
        
        this.reportDurationFieldType=reportDurationFieldType;
        this.reportDurationFieldValue=reportPeriodValue;
        reportPeriod=getMutablePeriod(reportDurationFieldType,reportPeriodValue );
        
        this.reportMarkerDurationFieldType=reportMarkerDurationFieldType;
        this.reportMarkerDurationFieldValue=reportMarkerPeriodValue;
        reportMarkerPeriod=getMutablePeriod(reportMarkerDurationFieldType, reportMarkerPeriodValue);
        
        
    }
    
    public static MutablePeriod getMutablePeriod(DurationFieldType durationFieldType, int value) {
        MutablePeriod mutablePeriod=new MutablePeriod();
        mutablePeriod.set(durationFieldType, value);
        return mutablePeriod;
    }

    public static Period getPeriod(DurationFieldType durationFieldType, int value) {
        return getMutablePeriod(durationFieldType, value).toPeriod();
    }

    public DurationFieldType getReportDurationFieldType() {
        return reportDurationFieldType;
    }
    
    public int getReportDurationFieldValue() {
        return reportDurationFieldValue;
    }
    
    public void setReportPeriod(MutablePeriod reportPeriod) {
        this.reportPeriod=reportPeriod;
    }
    
    public MutablePeriod getReportPeriod() {
        return reportPeriod;
    }

    public DurationFieldType getIntervalDurationFieldType() {
        return intervalDurationFieldType;
    }

    public MutablePeriod getIntervalPeriod() {
        return intervalPeriod;
    }

    public DurationFieldType getCandleDurationFieldType() {
        return candleDurationFieldType;
    }

    public MutablePeriod getCandlePeriod() {
        return candlePeriod;
    }

    public DurationFieldType getReportMarkerDurationFieldType() {
        return reportMarkerDurationFieldType;
    }

    public MutablePeriod getReportMarkerPeriod() {
        return reportMarkerPeriod;
    }
    
    public int getReportMarkerDurationFieldValue() {
        return reportMarkerDurationFieldValue;
    }

    public void setReportMarkerPeriod(MutablePeriod reportMarkerPeriod) {
        this.reportMarkerPeriod = reportMarkerPeriod;
    }
    
    public String toString() {
	PeriodFormatter fmt = new PeriodFormatterBuilder()
	    .printZeroNever()
	    .appendYears()
	    .appendSuffix("Jahr", "Jahre")
	    .printZeroNever()
	    .appendMonths()
	    .appendSuffix("Monat", "Monate")
	    .printZeroNever()
	    .appendWeeks()
	    .appendSuffix("Woche", "Wochen")
	    .printZeroNever()
	    .appendDays()
	    .appendSuffix("Tag", "Tage")
	    .printZeroNever()
	    .appendHours()
	    .appendSuffix("Stunde", "Stunden")
	    .printZeroNever()
	    .appendMinutes()
	    .appendSuffix("Minute", "Minuten")
	    .toFormatter();
	
        StringBuffer stringBuffer = new StringBuffer();
        fmt.printTo(stringBuffer, reportPeriod);
        stringBuffer.append("(");
        fmt.printTo(stringBuffer, intervalPeriod);
        stringBuffer.append("; ");
        fmt.printTo(stringBuffer, candlePeriod);
        stringBuffer.append(")");
	
        return stringBuffer.toString();
    }

    public String getDescription() {
	PeriodFormatter fmt = new PeriodFormatterBuilder()
	    .printZeroNever()
	    .appendYears()
	    .appendSuffix("y")
	    .printZeroNever()
	    .appendMonths()
	    .appendSuffix("m")
	    .printZeroNever()
	    .appendWeeks()
	    .appendSuffix("w")
	    .printZeroNever()
	    .appendDays()
	    .appendSuffix("d")
	    .printZeroNever()
	    .appendHours()
	    .appendSuffix("s")
	    .printZeroNever()
	    .appendMinutes()
	    .appendSuffix("m")
	    .toFormatter();

        StringBuffer stringBuffer = new StringBuffer();
        fmt.printTo(stringBuffer, reportPeriod);
        stringBuffer.append("; ");
        fmt.printTo(stringBuffer, candlePeriod);

        return stringBuffer.toString();
    }

    public String getCaption() {
	PeriodFormatter fmt = new PeriodFormatterBuilder()
	    .printZeroNever()
	    .appendYears()
	    .appendSuffix("y")
	    .printZeroNever()
	    .appendMonths()
	    .appendSuffix("m")
	    .printZeroNever()
	    .appendWeeks()
	    .appendSuffix("w")
	    .printZeroNever()
	    .appendDays()
	    .appendSuffix("d")
	    .printZeroNever()
	    .appendHours()
	    .appendSuffix("s")
	    .printZeroNever()
	    .appendMinutes()
	    .appendSuffix("m")
	    .toFormatter();

        StringBuffer stringBuffer = new StringBuffer();
        fmt.printTo(stringBuffer, reportPeriod);
        stringBuffer.append(" ");
        fmt.printTo(stringBuffer, candlePeriod);

        return stringBuffer.toString();
    }
}

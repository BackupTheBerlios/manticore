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

import com.manticore.foundation.Candle;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.manticore.foundation.TimeMarker;
import com.manticore.database.Quotes;
import com.manticore.foundation.Extremum;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import com.manticore.foundation.Tick;
import com.manticore.foundation.Transaction;
import com.manticore.indicators.ADXR;
import com.manticore.indicators.ATR;
import com.manticore.indicators.AroonOscillator;
import com.manticore.indicators.BollingerBands;
import com.manticore.indicators.ChaikinOscillator;
import com.manticore.indicators.MACD;
import com.manticore.indicators.MinusDI;
import com.manticore.indicators.MovingAverage;
import com.manticore.indicators.ParabolicSAR;
import com.manticore.indicators.PlusDI;
import com.manticore.indicators.SlowStochasticRSI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.joda.time.MutablePeriod;
import org.joda.time.format.PeriodFormat;
import com.manticore.util.ThreadArrayList;
import java.util.ArrayList;

/**
 *
 * @author are
 */
public class CandleArrayList extends ArrayList<Candle> {
    private final static Logger logger=Logger.getLogger(CandleArrayList.class.getName());

    //private DataSource datasource;
    private Instrument instrument;
    private StockExchange stockExchange;
    private DateTime dateTimeTo;
    private PeriodSettings periodSettings;
    private boolean hasQuantity = false;
    long width = 0;
    private Float top = null;
    private Float bottom = null;
    private Long maxquantity = Long.valueOf(0);
    public Double maxMACD = null;
    public Double maxAroonOscilator = null;
    public Double minAroonOscilator = null;
    public Double maxChaikinOscilator = null;
    public Double minChaikinOscilator = null;
    public Double maxATR = null;
    public Double minATR = null;
    private ArrayList<TimeMarker> timeMarkerArrayList;
    private ArrayList<Transaction> transactionMarkerArrayList;
    private ExtremumArrayList extremumArrayList;

    public CandleArrayList(ChartParameters chartParameters) {
        this.instrument = chartParameters.getInstrument();
        this.stockExchange = chartParameters.getStockExchange();
        this.dateTimeTo = chartParameters.getDateTimeTo();
        this.periodSettings = chartParameters.getPeriodSettings();

        this.hasQuantity = chartParameters.isHasQuantity();

        if (chartParameters.isEod()) {
            calculateEODCandles();
        } else {
            calculateTradingTimeUnits();
        }
    }

    private boolean addCandle(Candle c) {
        if (!c.isFirst()) {
            if (top == null || bottom == null) {
                top = c.getHigh();
                bottom = c.getLow();
            } else {
                if (top < c.getHigh()) {
                    top = c.getHigh();
                }
                if (bottom > c.getLow()) {
                    bottom = c.getLow();
                }
            }

            if (c.getQuantity() > getMaxquantity()) {
                maxquantity = c.getQuantity();
            }
        }
        return add(c);
    }

    public Candle firstElement() {
        Candle candle = null;

        for (int i = 0; i < size() && candle == null; i++) {
            if (!get(i).isFirst()) {
                candle = get(i);
            }
        }

        return candle;
    }

    private void calculateTradingTimeUnits() {
        clear();

        ResultSet rs;
        MutableDateTime dateTimeFrom = getDateTimeFrom();

        timeMarkerArrayList = Quotes.getInstance().getTimeMarkerArrayList(dateTimeFrom.toDateTime(), dateTimeTo);
        transactionMarkerArrayList=Quotes.getInstance().getTransactionArrayList(instrument.id_instrument, dateTimeFrom.toDateTime(), dateTimeTo);
        extremumArrayList=new ExtremumArrayList();
        
        Duration candleDuration = getPeriodSettings().getCandlePeriod().toDurationFrom(dateTimeFrom);
        long increment = 1;

        if (getPeriodSettings().getCandleDurationFieldType().equals(DurationFieldType.minutes()) || getPeriodSettings().getCandleDurationFieldType().equals(DurationFieldType.hours())) {
            increment = (stockExchange.getClosingMinute() - stockExchange.getOpeningMinute()) * 60000L / candleDuration.getMillis();
        }

        
        int lastHighIndex = 0;
        int lastLowIndex = 0;
        int mode = 0;
        int candlePos=0;
        Float lastHigh = null;
        Float lastLow = null;
        Float previousHigh=null;
        Float previousLow=null;

        try {
            rs = Quotes.getInstance().getTickdataResultSet(instrument.getId(), stockExchange.getId(), dateTimeFrom.toDate(), dateTimeTo.toDate());
            boolean doLoop = rs.next();

            while (!dateTimeFrom.isAfter(dateTimeTo)) {
                if (stockExchange.isBusinessDay(dateTimeFrom)) {

                    MutableDateTime candleStartDateTime = dateTimeFrom.copy();
                    candleStartDateTime.setMinuteOfDay(stockExchange.getOpeningMinute());
                    candleStartDateTime.setSecondOfMinute(0);
                    candleStartDateTime.setMillisOfSecond(0);

                    for (int i = 0; i < increment; i++) {
                        if (stockExchange.isHoliday(candleStartDateTime)) {
                        } else {

                            Candle candle = new Candle(candleStartDateTime, getPeriodSettings().getCandlePeriod(), size());

                            candleStartDateTime.add(getPeriodSettings().getCandlePeriod());

                            while (doLoop && candle.getStart().isAfter(new DateTime(rs.getTimestamp("timestamp")))) {
                                doLoop = rs.next();
                            }

                            while (doLoop && candle.containsDate(rs.getTimestamp("timestamp"))) {
                                //candle.addValues(rs.getFloat("opening"), rs.getFloat("closing"), rs.getFloat("top"), rs.getFloat("bottom"), rs.getLong("quantity"));
                                candle.addTick(rs.getFloat("price"), rs.getLong("quantity"));
                                doLoop = rs.next();
                            }


                            addCandle(candle);
                            if (candle.isFirst()) {
                                mode=0;
                            }
                            if (mode == 0) {
                                if (lastLow == null || (candle.getLow() != null && lastLow > candle.getLow())) {
                                    previousLow=lastLow;
                                    lastLow = candle.getLow();
                                    lastLowIndex = candlePos;
                                } else if (candle.getHigh() != null && get(candlePos - 1)!=null && get(candlePos - 1).getHigh()!=null && get(candlePos - 1).getHigh() < candle.getHigh()) {
                                    previousHigh=lastHigh;
                                    lastHigh = candle.getHigh();
                                    lastHighIndex = candlePos;
                                    get(lastLowIndex).setLocalExtremum(Candle.LOCAL_EXTREMUM_LOW);

                                    extremumArrayList.add(new Extremum(get(lastLowIndex).getEnd(), get(lastLowIndex).getLow(), Extremum.TYPE_EXTREMUM_TMP_LOW));
                                    mode = 1;
                                }
                            } else {
                                if (lastHigh == null || (candle.getHigh() != null && lastHigh < candle.getHigh())) {
                                    previousHigh=lastHigh;
                                    lastHigh = candle.getHigh();
                                    lastHighIndex = candlePos;
                                } else if (candle.getLow() != null && get(candlePos - 1) != null && get(candlePos - 1).getLow() != null && get(candlePos - 1).getLow() > candle.getLow()) {
                                    previousLow=lastLow;
                                    lastLow = candle.getLow();
                                    lastLowIndex = candlePos;
                                    get(lastHighIndex).setLocalExtremum(Candle.LOCAL_EXTREMUM_HIGH);

                                    extremumArrayList.add(new Extremum(get(lastHighIndex).getEnd(), get(lastHighIndex).getHigh(), Extremum.TYPE_EXTREMUM_TMP_HIGH));
                                    mode = 0;
                                }
                            }

                            candlePos++;
                        }
                    }

                }
                dateTimeFrom.add(getPeriodSettings().getIntervalPeriod());
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        extremumArrayList.adjustExtremum();
        calculateIndices();
    }

    private void calculateEODCandles() {
        clear();

        ResultSet rs;
        MutableDateTime dateTimeFrom = getDateTimeFrom();

        timeMarkerArrayList = Quotes.getInstance().getTimeMarkerArrayList(dateTimeFrom.toDateTime(), dateTimeTo);

        Duration candleDuration = getPeriodSettings().getCandlePeriod().toDurationFrom(dateTimeFrom);
        long increment = 1;

        if (getPeriodSettings().getCandleDurationFieldType().equals(DurationFieldType.minutes()) || getPeriodSettings().getCandleDurationFieldType().equals(DurationFieldType.hours())) {
            increment = (stockExchange.getClosingMinute() - stockExchange.getOpeningMinute()) * 60000L / candleDuration.getMillis();
        }

        try {
            rs = Quotes.getInstance().getEODResultSet(instrument.getIsin(), dateTimeFrom.toDate(), dateTimeTo.toDate());
            boolean doLoop = rs.next();

            while (!dateTimeFrom.isAfter(dateTimeTo)) {
                if (getStockExchange().isBusinessDay(dateTimeFrom)) {

                    MutableDateTime candleStartDateTime = dateTimeFrom.copy();
                    candleStartDateTime.setMinuteOfDay(stockExchange.getOpeningMinute());
                    candleStartDateTime.setSecondOfMinute(0);
                    candleStartDateTime.setMillisOfSecond(0);

                    for (int i = 0; i < increment; i++) {
                        if (stockExchange.isHoliday(candleStartDateTime)) {
                        } else {

                            Candle candle = new Candle(candleStartDateTime, getPeriodSettings().getCandlePeriod(), size());

                            candleStartDateTime.add(getPeriodSettings().getCandlePeriod());

                            while (doLoop && candle.getStart().isAfter(new DateTime(rs.getDate("day")))) {
                                doLoop = rs.next();
                            }

                            while (doLoop && candle.containsDate(rs.getDate("day"))) {
                                Candle eodCandle = new Candle(rs.getDate("day"), rs.getFloat("open"), rs.getFloat("high"), rs.getFloat("low"), rs.getFloat("close"), rs.getLong("quantity"), rs.getFloat("close_adjusted"));

                                candle.addCandle(eodCandle);
                                doLoop = rs.next();
                            }


                            addCandle(candle);
                        }
                    }
                }
                dateTimeFrom.add(getPeriodSettings().getIntervalPeriod());
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        calculateIndices();
    }

    public int getTradingTimeUnits() {
        return size();
    }
    //@todo: move this into PeriodSettings

    public MutableDateTime getDateTimeFrom() {
        MutableDateTime dateTimeFrom = new MutableDateTime(dateTimeTo);
        int i = 0;

        while (i < getPeriodSettings().getReportDurationFieldValue()) {
            dateTimeFrom.add(getPeriodSettings().getReportDurationFieldType(), -1);

            if (stockExchange.isBusinessDay(dateTimeFrom)) {
                i++;
            }
        }

        if (dateTimeFrom.getMinuteOfDay() >= stockExchange.getClosingMinute()) {
            dateTimeFrom.addDays(1);
        }
        dateTimeFrom.setMinuteOfDay(stockExchange.getOpeningMinute());
        dateTimeFrom.setSecondOfMinute(0);
        dateTimeFrom.setMillisOfSecond(0);

        //@todo: move this into PeriodSettings
        MutablePeriod reportPeriod = new MutablePeriod(dateTimeFrom, dateTimeTo);
        getPeriodSettings().setReportMarkerPeriod(reportPeriod);
        return dateTimeFrom;
    }

    public DateTime getDateTimeTo() {
        return dateTimeTo;
    }

    public void setDateTimeTo(DateTime newDateTimeTo) {
        this.dateTimeTo = newDateTimeTo;
        calculateTradingTimeUnits();
    }

    public MutablePeriod getReportPeriod() {
        return getPeriodSettings().getReportPeriod();
    }

    public MutablePeriod getReportMarkerPeriod() {
        return getPeriodSettings().getReportMarkerPeriod();
    }

    public MutablePeriod getIntervalPeriod() {
        return getPeriodSettings().getIntervalPeriod();
    }

    public String getSymbol() {
        return getInsrument().getSymbol();
    }

    public PeriodSettings getPeriodSettings() {
        return periodSettings;
    }

    public void setPeriodSettings(PeriodSettings periodSettings) {
        this.periodSettings = periodSettings;
        calculateTradingTimeUnits();
    }

    public StockExchange getStockExchange() {
        return stockExchange;
    }

    public String getStockExchangeSymbol() {
        return stockExchange.getSymbol();
    }

    public void setStockExchange(StockExchange stockExchange) {
        this.stockExchange = stockExchange;
        this.hasQuantity = Quotes.getInstance().hasQuantity(instrument.getId(), stockExchange.getId());
    }

    public String getLabel() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(stockExchange.getName()).append(".");
        stringBuffer.append(getInsrument().getSymbol()).append(":");
        stringBuffer.append(PeriodFormat.getDefault().print(periodSettings.getReportPeriod())).append(",");
        stringBuffer.append(PeriodFormat.getDefault().print(periodSettings.getCandlePeriod())).append(",");
        return stringBuffer.toString();
    }

    public Candle getCandle(DateTime dateTime) {
        Candle candle = null;
        for (int i = size() - 1; i > 0; i--) {
            Candle c = get(i);
            if (c.containsDateTime(dateTime)) {
                candle = c;
                i = 0;
            }
        }
        return candle;
    }

	 public Candle getCandleFromTick(Tick tick) {
        Candle candle = getCandle(tick.getDateTime());
        if (candle != null) {
            candle.addTick(tick.getPrice(), tick.getQuantity());
            calculateIndices();
        } else {
            Logger.getLogger(getClass().getName()).finest("Candle for tick not found!" + tick.getDateTime());
        }
        return candle;
    }

    public Candle getCandleFromTick(DateTime dateTime, float price) {
        Candle candle = getCandle(dateTime);
        if (candle != null) {
            candle.addTick(price, 0);
            calculateIndices();
        } else {
            Logger.getLogger(getClass().getName()).finest("Candle for tick not found!".concat(dateTime.toString()));
        }
        return candle;
    }

    public Candle getLastValidCandle() {
        Candle c = null;
        for (int i = size() - 1; c == null && i > 0; i--) {
            if (get(i)!=null && !get(i).isFirst()) c = get(i);
        }
        return c;
    }

    public Candle getFirstValidCandle() {
        Candle c = null;
        for (int i = 0; c == null && i < size(); i++) {
            c = get(i);
        }
        return c;
    }

    private void calculateIndices() {
        int k1 = -1;
        int k2 = -1;
        Core talib = new Core();
        for (int l = 0; l < size(); l++) {

            if (!get(l).isFirst() && k1 == -1) {
                k1 = l;
            } else if (!get(l).isFirst() && l == size() - 1 && k1 > -1) {
                k2 = l;
                calculateIndicators2(talib, k1, k2);
                k1 = -1;
                k2 = -1;
            } else if (get(l).isFirst() && k1 > -1 && l > 1) {
                k2 = l - 1;
                calculateIndicators2(talib, k1, k2);
                k1 = -1;
                k2 = -1;
            }

        }
    }

    private void calculateIndicators2(Core talib, int k1, int k2) {
        double[] inHigh = new double[k2 - k1+1];
        double[] inLow = new double[k2 - k1+1];
        double[] inClosing = new double[k2 - k1+1];
        double[] inQuanity = new double[k2 - k1+1];

        for (int i = k1; i <= k2; i++) {
            inHigh[i - k1] = get(i).getHigh().doubleValue();
            inLow[i - k1] = get(i).getLow().doubleValue();
            inClosing[i - k1] = get(i).getClosing().doubleValue();
            inQuanity[i - k1] = get(i).getQuantity().doubleValue();
        }

        ThreadArrayList threadArrayList = new ThreadArrayList(12);

        threadArrayList.addThread(new MovingAverage(this, k1, k2, talib, inClosing, 30));
        threadArrayList.addThread(new ParabolicSAR(this, k1, k2, talib, inHigh, inLow, 0.02, 2));
        threadArrayList.addThread(new MACD(this, k1, k2, talib, inClosing, 26, 12, 9));
        //threadArrayList.addThread(new FastStochasticRSI(this, k1, k2, talib, inClosing, 14, 5, 3, MAType.T3));
        threadArrayList.addThread(new SlowStochasticRSI(this, k1, k2, talib, inClosing, 84, 5, 3, MAType.T3));
        threadArrayList.addThread(new AroonOscillator(this, k1, k2, talib, inHigh, inLow, 18));

        if (isHasQuantity()) {
            threadArrayList.addThread(new ChaikinOscillator(this, k1, k2, talib, inHigh, inLow, inClosing, inQuanity, 3, 10));
        }
        threadArrayList.addThread(new ADXR(this, k1, k2, talib, inHigh, inLow, inClosing, 14));
        threadArrayList.addThread(new MinusDI(this, k1, k2, talib, inHigh, inLow, inClosing, 14));
        threadArrayList.addThread(new PlusDI(this, k1, k2, talib, inHigh, inLow, inClosing, 14));
        threadArrayList.addThread(new ATR(this, k1, k2, talib, inHigh, inLow, inClosing, 7));
        ;
        threadArrayList.addThread(new BollingerBands(this, k1, k2, talib, inClosing, 7, 2.5, 2.5, MAType.T3));

        threadArrayList.join();
    }

    //public DataSource getDatasource() {
    //    return datasource;
    //}
    /**
     * @return the instrument
     */
    public Instrument getInsrument() {
        return instrument;
    }

    /**
     * @param instrument the instrument to set
     */
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;

        this.hasQuantity = Quotes.getInstance().hasQuantity(instrument.getId(), stockExchange.getId());
    }

    /**
     * @return the hasQuantity
     */
    public boolean isHasQuantity() {
        return hasQuantity;
    }

    /**
     * @return the maxquantity
     */
    public Long getMaxquantity() {
        return maxquantity;
    }

    /**
     * @return the timeMarkerArrayList
     */
    public ArrayList<TimeMarker> getTimeMarkerArrayList() {
        return timeMarkerArrayList;
    }

    /**
     * @return the transactionMarkerArrayList
     */
    public ArrayList<Transaction> getTransactionMarkerArrayList() {
        return transactionMarkerArrayList;
    }

    /**
     * @param transactionMarkerArrayList the transactionMarkerArrayList to set
     */
    public void setTransactionMarkerArrayList(ArrayList<Transaction> transactionMarkerArrayList) {
        this.transactionMarkerArrayList = transactionMarkerArrayList;
    }

    /**
     * @return the top
     */
    public Float getTop() {
        return top;
    }

    /**
     * @return the bottom
     */
    public Float getBottom() {
        return bottom;
    }

    /**
     * @return the extremumArrayList
     */
    public ExtremumArrayList getExtremumArrayList() {
        return extremumArrayList;
    }
}

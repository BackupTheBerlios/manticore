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

import com.manticore.util.Settings;
import java.awt.Color;
import java.util.Date;
import org.joda.time.*;

public class Candle {

    private Interval interval;
    private Float opening = null;
    private Float closing = null;
    private Float high = null;
    private Float low = null;
    private Long quantity = 0l;
    private int position;
    private Double movingAverage = null;
    private Double parabolicSAR = null;
    private Double macd = null;
    private Double macdSignal = null;
    private Double macdHist = null;
    private Double stochasticRSI_FastK = null;
    private Double stochasticRSI_FastD = null;
    private Double stochasticRSI_SlowK = null;
    private Double stochasticRSI_SlowD = null;
    private Double chaikainOszilator = null;
    private Double aroonOszilator = null;
    private Double adxr = null;
    private Double minusDI = null;
    private Double plusDI = null;
    private Double bbUpperBand = null;
    private Double bbMiddleBand = null;
    private Double bbLowerBand = null;
    private Integer patternScore = null;
    private Double averageTrueRange = null;
    public Float highLowFilter=null;
    
    boolean first = true;

    public static int LOCAL_EXTREMUM_HIGH=1;
    public static int LOCAL_EXTREMUM_NO=0;
    public static int LOCAL_EXTREMUM_LOW=-1;

    private int localExtremum=LOCAL_EXTREMUM_NO;

    public Candle(DateTime dt, MutablePeriod period, int p) {
        interval = new Interval(dt, period);
        position = p;
    }

    public Candle(Date dt, float open, float high, float low, float close, long quantity, float adjusted_close) {
        interval=new Interval(new DateTime(dt), new MutablePeriod(PeriodType.days()).toPeriod() );
        this.opening=open;
        this.high=high;
        this.low=low;
        this.closing=close;
        this.quantity=quantity;
    }

    public Candle(MutableDateTime dt, MutablePeriod period, int p) {
        interval = new Interval(dt, period);
        position = p;
    }

    public Candle(long start, MutablePeriod period, int p, float opening, float closing, float top, float bottom, long quantity) {
        interval = new Interval(new DateTime(start * 1000L), period);
        position = p;

        this.opening = Float.valueOf(opening);
        this.closing = Float.valueOf(closing);
        this.high = Float.valueOf(top);
        this.low = Float.valueOf(bottom);
        this.quantity = Long.valueOf(quantity);
    }

    public void addTick(float value, long quantity) {
        if (isFirst()) {
            setOpening(value);
            setLow(value);
            setHigh(value);
        } else {
            if (getLow() > value) {
                setLow(value);
            }
            if (getHigh() < value) {
                setHigh(value);
            }
        }
        setClosing(value);
        setQuantity((Long) (getQuantity() + quantity));

        first = false;
    }

    public void addValues(float opening, float closing, float high, float low, long quantity) {
        if (isFirst()) {
            setOpening(opening);
            setLow(low);
            setHigh(high);
        } else {
            if (getLow() > low) {
                setLow(low);
            }
            if (getHigh() < high) {
                setHigh(high);
            }
        }
        setClosing(closing);
        setQuantity((Long) (getQuantity() + quantity));

        first = false;
    }

    public void addCandle(Candle c) {
        if (isFirst()) {
            setOpening(c.getOpening());
            setLow(c.getLow());
            setHigh(c.getHigh());
        } else {
            if (getLow() > c.getLow()) {
                setLow(c.getLow());
            }
            if (getHigh() < c.getHigh()) {
                setHigh(c.getHigh());
            }
        }
        setClosing(c.getClosing());
        setQuantity((Long) (getQuantity() + c.getQuantity()));

        first = false;
    }

    public Color getColor() {
        return (getOpening() > getClosing()) ? Settings.MANTICORE_ORANGE : Settings.MANTICORE_DARK_BLUE;
    }

    public int getPosition() {
        return position;
    }

    public DateTime getStart() {
        return getInterval().getStart();
    }

    public DateTime getEnd() {
        return getInterval().getEnd();
    }

    public Float getOpening() {
        return opening;
    }

    public void setOpening(Float opening) {
        this.opening = opening;
    }

    public Float getClosing() {
        return closing;
    }

    public void setClosing(Float closing) {
        this.closing = closing;
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Float getLow() {
        return low;
    }

    public void setLow(Float low) {
        this.low = low;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean containsSeconds(long seconds) {
        long millis = seconds * 1000L;
        return (getInterval().getStartMillis() <= millis && getInterval().getEndMillis() >= millis);
    }
    /*
    public boolean containsMillis(long millis) {
        return (getInterval().contains(arg0) <= millis && getInterval().getEndMillis() >= millis);
    }
    */
    public boolean containsDateTime(DateTime dateTime) {
        return (getInterval().contains(dateTime));
    }
    
    public boolean containsDate(Date date) {
        return (getInterval().contains(date.getTime()));
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Double getMovingAverage() {
        return movingAverage;
    }

    public void setMovingAverage(double movingAverage) {
        this.movingAverage = Double.valueOf(movingAverage);
    }

    public Double getParabolicSAR() {
        return parabolicSAR;
    }

    public void setParabolicSAR(double parabolicSAR) {
        this.parabolicSAR = Double.valueOf(parabolicSAR);
    }

    public Double getMacd() {
        return macd;
    }

    public Double getMacdSignal() {
        return macdSignal;
    }

    public Double getMacdHist() {
        return macdHist;
    }

    public void setMacd(double macd, double macdSignal, double macdHist) {
        this.macd = Double.valueOf(macd);
        this.macdSignal = Double.valueOf(macdSignal);
        this.macdHist = Double.valueOf(macdHist);
    }

    public Double getStochasticRSI_FastD() {
        return stochasticRSI_FastD;
    }

    public Double getStochasticRSI_FastK() {
        return stochasticRSI_FastK;
    }

    public void setFastStochasticRSI(double stochasticRSI_FastK, double stochasticRSI_FastD) {
        this.stochasticRSI_FastK = Double.valueOf(stochasticRSI_FastK);
        this.stochasticRSI_FastD = Double.valueOf(stochasticRSI_FastD);
    }

    public void setSlowStochasticRSI(double stochasticRSI_SlowK, double stochasticRSI_SlowD) {
        this.stochasticRSI_SlowK = Double.valueOf(stochasticRSI_SlowK);
        this.stochasticRSI_SlowD = Double.valueOf(stochasticRSI_SlowD);
    }

    public Double getChaikainOszilator() {
        return chaikainOszilator;
    }

    public void setChaikainOszilator(double chaikainOszilator) {
        this.chaikainOszilator = Double.valueOf(chaikainOszilator);
    }

    public Double getAroonOszilator() {
        return aroonOszilator;
    }

    public void setAroonOszilator(double aroonOszilator) {
        this.aroonOszilator = Double.valueOf(aroonOszilator);
    }

    public Double getAdxr() {
        return adxr;
    }

    public void setAdxr(Double adxr) {
        this.adxr = adxr;
    }

    public Double getMinusDI() {
        return minusDI;
    }

    public void setMinusDI(Double minusDI) {
        this.minusDI = minusDI;
    }

    public Double getPlusDI() {
        return plusDI;
    }

    public void setPlusDI(Double plusDI) {
        this.plusDI = plusDI;
    }

    public void setBB(Double bbUpperBand, Double bbMiddleBand, Double bbLowerBand) {
        this.bbUpperBand = bbUpperBand;
        this.bbMiddleBand = bbMiddleBand;
        this.bbLowerBand = bbLowerBand;
    }

    public Double getBbUpperBand() {
        return bbUpperBand;
    }

    public Double getBbMiddleBand() {
        return bbMiddleBand;
    }

    public Double getBbLowerBand() {
        return bbLowerBand;
    }

    public Integer getPatternScore() {
        return patternScore;
    }

    public void plusPatternScore(Integer patternScore) {
        if (this.patternScore == null) {
            this.patternScore = patternScore;
        } else {
            this.patternScore += patternScore;
        }
    }

    public void minusPatternScore(Integer patternScore) {
        if (this.patternScore == null) {
            this.patternScore = -patternScore;
        } else {
            this.patternScore -= patternScore;
        }
    }

    public Double getAverageTrueRange() {
        return averageTrueRange;
    }

    public void setAverageTrueRange(Double averageTrueRange) {
        this.averageTrueRange = averageTrueRange;
    }

    /**
     * @return the stochasticRSI_SlowK
     */
    public Double getStochasticRSI_SlowK() {
        return stochasticRSI_SlowK;
    }

    /**
     * @param stochasticRSI_SlowK the stochasticRSI_SlowK to set
     */
    public void setStochasticRSI_SlowK(Double stochasticRSI_SlowK) {
        this.stochasticRSI_SlowK = stochasticRSI_SlowK;
    }

    /**
     * @return the stochasticRSI_SlowD
     */
    public Double getStochasticRSI_SlowD() {
        return stochasticRSI_SlowD;
    }

    /**
     * @param stochasticRSI_SlowD the stochasticRSI_SlowD to set
     */
    public void setStochasticRSI_SlowD(Double stochasticRSI_SlowD) {
        this.stochasticRSI_SlowD = stochasticRSI_SlowD;
    }

    /**
     * @return the localExtremum
     */
    public int getLocalExtremum() {
        return localExtremum;
    }

    /**
     * @param localExtremum the localExtremum to set
     */
    public void setLocalExtremum(int localExtremum) {
        this.localExtremum = localExtremum;
    }
}

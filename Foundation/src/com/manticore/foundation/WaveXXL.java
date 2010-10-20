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

public class WaveXXL {
    private String wkn="";
    private Float bid;
    private Float ask;
    private Float strike;
    private Float ko;
    private Float leverage;
    private Float multiple;

    public WaveXXL(String wkn, Float bid, Float ask, Float strike,
            Float ko, Float leverage, Float multiple) {

        this.wkn=wkn;

        this.bid=bid;
        this.ask=ask;
        this.strike=strike;
        this.ko=ko;
        this.leverage=leverage;
        this.multiple=multiple;
    }

    /**
     * @return the wkn
     */
    public String getWkn() {
        return wkn;
    }

    /**
     * @param wkn the wkn to set
     */
    public void setWkn(String wkn) {
        this.wkn = wkn;
    }

    /**
     * @return the bid
     */
    public Float getBid() {
        return bid;
    }

    /**
     * @param bid the bid to set
     */
    public void setBid(Float bid) {
        this.bid = bid;
    }

    /**
     * @return the ask
     */
    public Float getAsk() {
        return ask;
    }

    /**
     * @param ask the ask to set
     */
    public void setAsk(Float ask) {
        this.ask = ask;
    }

    public Float getSpread() {
        return ask.floatValue()-bid.floatValue();
    }

    /**
     * @return the strike
     */
    public Float getStrike() {
        return strike;
    }

    /**
     * @param strike the strike to set
     */
    public void setStrike(Float strike) {
        this.strike = strike;
    }

    /**
     * @return the ko
     */
    public Number getKo() {
        return ko;
    }

    /**
     * @param ko the ko to set
     */
    public void setKo(Float ko) {
        this.ko = ko;
    }

    /**
     * @return the leverage
     */
    public Number getLeverage() {
        return leverage;
    }

    /**
     * @param leverage the leverage to set
     */
    public void setLeverage(Float leverage) {
        this.leverage = leverage;
    }

    /**
     * @return the ratio
     */
    public Float getMultiple() {
        return multiple;
    }

    /**
     * @param ratio the ratio to set
     */
    public void setMultiple(Float multiple) {
        this.multiple = multiple;
    }

    //@todo: implement isin instead of wkn

    /**
     * @return the isin
     */
    public String getIsin() {
        return wkn;
    }

    /**
     * @param isin the isin to set
     */
    public void setIsin(String isin) {
        this.wkn = isin;
    }
}

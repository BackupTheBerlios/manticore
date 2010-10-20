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

import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import com.manticore.chart.annotation.Annotation;
import com.manticore.database.Quotes;
import java.util.ArrayList;
import javax.swing.JToggleButton;
import org.joda.time.DateTime;

public class ChartParameters {

    private Instrument instrument;
    private StockExchange stockExchange;
    private DateTime dateTimeTo;
    private PeriodSettings periodSettings;
    private int treeItem;
    private ArrayList<Annotation> figurelist;
    private JToggleButton jToggleButton;
    private boolean eod = false;
    private Float adjustedMaxPrice = null;
    private Float adjustedMinPrice = null;

    public ChartParameters(int treeItem, Instrument instrument, StockExchange stockExchange, DateTime dateTimeTo, PeriodSettings periodSettings) {
        this.treeItem = treeItem;
        this.instrument = instrument;
        this.stockExchange = stockExchange;
        this.dateTimeTo = dateTimeTo;
        this.periodSettings = periodSettings;


        figurelist = new ArrayList();
    }

    /**
     * @return the instrument
     */
    public Instrument getInstrument() {
        return instrument;
    }

    /**
     * @param instrument the instrument to set
     */
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
        updateButtonText();
    }

    /**
     * @return the stockExchange
     */
    public StockExchange getStockExchange() {
        return stockExchange;
    }

    /**
     * @param stockExchange the stockExchange to set
     */
    public void setStockExchange(StockExchange stockExchange) {
        this.stockExchange = stockExchange;
        updateButtonText();
    }

    public boolean updateInstrument(Instrument instrument, StockExchange stockExchange, int treeItem) {
        boolean updateInstrument=!(this.instrument.equals(instrument) && this.stockExchange.equals(stockExchange));

        this.stockExchange=stockExchange;
        this.instrument=instrument;
        this.treeItem=treeItem;
        updateButtonText();

        return updateInstrument;
    }

    /**
     * @return the dateTimeTo
     */
    public DateTime getDateTimeTo() {
        return dateTimeTo;
    }

    /**
     * @param dateTimeTo the dateTimeTo to set
     */
    public void setDateTimeTo(DateTime dateTimeTo) {
        this.dateTimeTo = dateTimeTo;
    }

    /**
     * @return the periodSettings
     */
    public PeriodSettings getPeriodSettings() {
        return periodSettings;
    }

    /**
     * @param periodSettings the periodSettings to set
     */
    public void setPeriodSettings(PeriodSettings periodSettings) {
        this.periodSettings = periodSettings;
        updateButtonText();
    }

    /**
     * @return the hasQuantity
     */
    public boolean isHasQuantity() {
        boolean hasQuantity = Quotes.getInstance().hasQuantity(instrument.getId(), stockExchange.getId());
        return hasQuantity;
    }

    /**
     * @return the figurelist
     */
    public ArrayList<Annotation> getFigurelist() {
        return figurelist;
    }

    /**
     * @param figurelist the figurelist to set
     */
    public void setFigurelist(ArrayList<Annotation> figurelist) {
        this.figurelist = figurelist;
    }

    /**
     * @return the treeItem
     */
    public int getTreeItem() {
        return treeItem;
    }

    /**
     * @param treeItem the treeItem to set
     */
    public void setTreeItem(int treeItem) {
        this.treeItem = treeItem;
    }

    /**
     * @return the jButton
     */
    public JToggleButton getButton() {
        return jToggleButton;
    }

    /**
     * @param jButton the jButton to set
     */
    public void setToogleButton(JToggleButton button) {
        this.jToggleButton = button;
        updateButtonText();
    }

    public String getDescription() {
        return stockExchange.getSymbol() + "." + instrument.getSymbol() + " (" + periodSettings.getDescription() + ") ";
    }

    public String getCaption() {
        return instrument.getSymbol() + " " + periodSettings.getCaption();
    }

    private void updateButtonText() {
        String caption = getCaption();
        if (instrument.getStockExchangeArrayList().size() > 1) {
            caption += " ▼";
        }

        if (jToggleButton != null) {
            jToggleButton.setText(caption);
            jToggleButton.setToolTipText(getDescription());
        }
    }

    /**
     * @return the eod
     */
    public boolean isEod() {
        return eod;
    }

    /**
     * @param eod the eod to set
     */
    public void setEod(boolean eod) {
        this.eod = eod;
    }

    public void resetMinMaxPrice() {
        adjustedMaxPrice = null;
        adjustedMinPrice = null;
    }

    public void setAdjustedMinMaxPrice(Float bottom, Float top) {
        if (bottom != null && top != null) {
            float t = 1000f / (float) Math.pow(10f, Math.ceil(Math.log10(bottom)));
            adjustedMaxPrice = (float) Math.ceil((top + 0.5 * (top - bottom)) * t) / t;
            adjustedMinPrice = (float) Math.floor((bottom - 0.5 * (top - bottom)) * t) / t;
        } else {
            resetMinMaxPrice();
        }
    }

    /**
     * @return the adjustedMaxPrice
     */
    public Float getAdjustedMaxPrice() {
        return adjustedMaxPrice;
    }

    /**
     * @param adjustedMaxPrice the adjustedMaxPrice to set
     */
    public void setAdjustedMaxPrice(Float adjustedMaxPrice) {
        this.adjustedMaxPrice = adjustedMaxPrice;
    }

    /**
     * @return the adjustedMinPrice
     */
    public Float getAdjustedMinPrice() {
        return adjustedMinPrice;
    }

    /**
     * @param adjustedMinPrice the adjustedMinPrice to set
     */
    public void setAdjustedMinPrice(Float adjustedMinPrice) {
        this.adjustedMinPrice = adjustedMinPrice;
    }
}

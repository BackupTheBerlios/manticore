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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Position {

    public final static String POSITION_TYPE_CALL = "C";
    public final static String POSITION_TYPE_PUT = "P";
    public final static String POSITION_STATUS_OPEN = "O";
    public final static String POSITION_STATUS_CLOSED = "C";
    public final static int MODE_FX_NONE = 0;
    public final static int MODE_FX_UNDERLYING = 1;
    public final static int MODE_FX_RATE = 2;
    private HashMap<String, Transaction> transactionHashMap = new HashMap<String, Transaction>();
    public long id_account;
    public long id_position;
    public Instrument instrument;
    public String id_position_type;
    public Long quantity = 0L;
    public Float averageEntry = 0f;
    public Float profit = 0f;
    private Float fxrStop = 1f;
    private Float fxrStopEntry = 1f;
    private Float fxrEntry = 1f;
    private Float fxrTarget = 1f;
    Float limit = -0.005f;
    Float targetRatio = -0.2f;
    Float targetTransactionAmount = 1200.00f;
    public Float underlyingStopLoss;
    public Float underlyingEntry;
    public Float underlyingTarget;
    private Float underlyingStopEntry; // for stop-limit orders only
    protected Float amount = 0f;
    protected Float marketPrice = 0f;
    private Float priceAdjustment = 0.001f;
    private WaveXXL waveXXL;
    public String isin;
    private Date lastUpdate=null;

    public Position() {
        
    }

    public Position(long id_account, long id_position, Instrument instrument) {
        this.id_account = id_account;
        this.id_position = id_position;
        this.instrument = instrument;
        setRiskParameters();
    }

    public Position(long id_account, long id_position, Instrument instrument, Float underlyingStop, Float underlyingEntry, Float underlyingTarget) {
        this.id_account = id_account;
        this.id_position = id_position;
        this.instrument = instrument;
        this.underlyingStopLoss = underlyingStop;
        this.underlyingEntry = underlyingEntry;
        this.underlyingTarget = underlyingTarget;

        derivePositionType();
        setRiskParameters();
    }

    public final void derivePositionType() {
        this.id_position_type = (underlyingStopLoss <= underlyingTarget) ? POSITION_TYPE_CALL : POSITION_TYPE_PUT;
    }

//    public Position(long id_instrument, Float targetRatio, Float targetTransactionAmount, Float underlyingStop, Float underlyingEntry, Float underlyingTarget) {
//
//        this.id_instrument = id_instrument;
//        this.targetRatio = targetRatio;
//        this.targetTransactionAmount = targetTransactionAmount;
//        this.underlyingStopLoss = underlyingStop;
//        this.underlyingEntry = underlyingEntry;
//        this.underlyingTarget = underlyingTarget;
//    }
    public float getMode() {
        return id_position_type.equals(POSITION_TYPE_PUT) ? -1f : 1f;
    }

    private void setRiskParameters() {
        try {
            limit = Float.valueOf(Settings.getInstance().get("manticore-trader", "PositionController", "limit"));
            targetRatio = Float.valueOf(Settings.getInstance().get("manticore-trader", "PositionController", "targetRatio"));
            targetTransactionAmount = Float.valueOf(Settings.getInstance().get("manticore-trader", "PositionController", "targetTransactionAmount"));
            priceAdjustment = Float.valueOf(Settings.getInstance().get("manticore-trader", "PositionController", "priceAdjustment"));
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not read risk parameters!", ex);
        }
    }

    public Float getUnderlyingStrike() {
        return (underlyingStopLoss + (targetRatio * (((underlyingStopLoss - underlyingTarget) / limit) - underlyingTarget))) / (1 - targetRatio);
    }

    public Float getRealUnderlyingEntry() {
        return (getMode() * getEntry() * getFxrEntry() / waveXXL.getMultiple()) + waveXXL.getStrike();
    }

    public Float getStopLoss() {
        return getMode() * (underlyingStopLoss - waveXXL.getStrike()) * waveXXL.getMultiple() / getFxrStop() - waveXXL.getSpread();
    }

    public void setStopLoss(Float stoppLoss) {
        underlyingStopLoss=(stoppLoss+waveXXL.getSpread())*getFxrStop()*getMode()/waveXXL.getMultiple() + waveXXL.getStrike();
    }

    public Float getEntry() {
        return getMode() * (underlyingEntry - waveXXL.getStrike()) * waveXXL.getMultiple() * (1 + priceAdjustment) / getFxrEntry() + waveXXL.getSpread();
    }

    public Float getStopEntry() {
        return getMode() * (underlyingStopEntry - waveXXL.getStrike()) * waveXXL.getMultiple() * (1 + priceAdjustment) / getFxrStopEntry() + waveXXL.getSpread();
    }

    public Float getOptimalEntry() {
        //@todo: simplify this term
        return (getMode() * (getMode() * (underlyingStopLoss - waveXXL.getStrike()) * waveXXL.getMultiple() / (limit + 1)) / waveXXL.getMultiple()) + waveXXL.getStrike();
    }

    public Float getTarget() {
        return getMode() * (underlyingTarget - waveXXL.getStrike()) * waveXXL.getMultiple() / getFxrTarget() + waveXXL.getSpread();
    }

    public Long getAvailableShares() {
        return Math.round(Math.ceil(targetTransactionAmount * 0.1f / getEntry()) * 10f);
    }

    public Float getTransactionAmount() {
        return getAvailableShares() * getEntry();
    }

    public Float getLoss() {
        return getAvailableShares() * (getStopLoss() - getEntry());
    }

    public Float getProfit() {
        return getAvailableShares() * (getTarget() - getEntry());
    }

    public float getRatio() {
        //return limit*(underlyingStop - waveXXL.getStrike() )/((limit*(underlyingTarget - waveXXL.getStrike())) - underlyingStop + underlyingTarget);
        return -1 * getLoss() / (getProfit() - getLoss());
    }

    public float getLimit() {
        return getLoss() / getTransactionAmount();
    }

    public String getDescription() {
        String description = "";

        if (waveXXL != null) {
            description = waveXXL.getWkn().concat(" (").concat(id_position_type).concat("), Leverage: ").concat(DecimalFormat.getInstance().format(waveXXL.getLeverage()));
        } else {
            Logger.getLogger(this.getClass().getName()).info("No WaveXXL set!");
        }

        return description;
    }

    public void buy(Float price, Long shares) {
        Float transactionAmount = price * shares;

        quantity += shares;
        amount += transactionAmount;

        averageEntry = amount / quantity;
    }

    public void sell(Float price, Long shares) {
        //@todo: implement some code for partial sale
        Float transactionAmount = price * shares;

        quantity += shares;
        amount += transactionAmount;

        profit += (averageEntry-price) * shares;
    }

    /**
     * @return the waveXXL
     */
    public WaveXXL getWaveXXL() {
        return waveXXL;
    }

    /**
     * @param waveXXL the waveXXL to set
     */
    public void setWaveXXL(WaveXXL waveXXL) {
        this.waveXXL = waveXXL;
    }

    /**
     * @return the quantity
     */
    public Long getQuantity() {
        return quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(Long heldShares) {
        this.quantity = heldShares;
    }

    /**
     * @return the averagePrice
     */
    public Float getAveragePrice() {
        return averageEntry;
    }

    /**
     * @param averagePrice the averagePrice to set
     */
    public void setAveragePrice(Float averageEntry) {
        this.averageEntry = averageEntry;
    }

    /**
     * @return the marketPrice
     */
    public Float getMarketPrice() {
        return marketPrice;
    }

    /**
     * @param marketPrice the marketPrice to set
     */
    public void setMarketPrice(Float marketPrice) {
        this.marketPrice = marketPrice;
    }

    /**
     * @return the amount
     */
    public Float getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(Float positionAmount) {
        this.amount = positionAmount;
    }

    /**
     * @return the positionProfit
     */
    public Float getPositionProfit() {
        Float totalProfit = 0f;

        if (marketPrice != null && averageEntry != null) {
            totalProfit += (marketPrice - averageEntry - waveXXL.getSpread()) * quantity;
        }

        if (profit != null) {
            totalProfit += profit;
        }
        return totalProfit;
    }

    /**
     * @return the underlyingStop
     */
    public Float getUnderlyingStopLoss() {
        return underlyingStopLoss;
    }

    /**
     * @param underlyingStop the underlyingStop to set
     */
    public void setUnderlyingStopLoss(Float underlyingStop) {
        this.underlyingStopLoss = underlyingStop;
    }

    /**
     * @return the underlyingEntry
     */
    public Float getUnderlyingEntry() {
        return underlyingEntry;
    }

    /**
     * @param underlyingEntry the underlyingEntry to set
     */
    public void setUnderlyingEntry(Float underlyingEntry) {
        this.underlyingEntry = underlyingEntry;
    }

    /**
     * @return the underlyingTarget
     */
    public Float getUnderlyingTarget() {
        return underlyingTarget;
    }

    /**
     * @param underlyingTarget the underlyingTarget to set
     */
    public void setUnderlyingTarget(Float underlyingTarget) {
        this.underlyingTarget = underlyingTarget;
    }

    /**
     * @return the priceAdjustment
     */
    public Float getAdjustment() {
        return priceAdjustment;
    }

    /**
     * @param priceAdjustment the priceAdjustment to set
     */
    public void setAdjustment(Float adjustment) {
        this.priceAdjustment = adjustment;
    }

    /**
     * @return the underlyingStopEntry
     */
    public Float getUnderlyingStopEntry() {
        return underlyingStopEntry;
    }

    /**
     * @param underlyingStopEntry the underlyingStopEntry to set
     */
    public void setUnderlyingStopEntry(Float underlyingStopEntry) {
        this.underlyingStopEntry = underlyingStopEntry;
    }

    /**
     * @param id_position_type the id_position_type to set
     */
    public void setId_position_type(String id_position_type) {
        this.id_position_type = id_position_type;
    }

    /**
     * @return the isin
     */
    public String getIsin() {
        return waveXXL!=null ? waveXXL.getIsin() : isin;
    }

    /**
     * @return the transactionVector
     */
    public HashMap<String, Transaction> getTransactionHashMap() {
        return transactionHashMap;
    }

    /**
     * @param transactionVector the transactionVector to set
     */
    public void setTransactionHashMap(HashMap<String, Transaction> transactionHashMap) {
        this.transactionHashMap = transactionHashMap;
    }

    /**
     * @return the fxrStop
     */
    public Float getFxrStop() {
        Float fxr = 1f;
        if (getFxMode() == MODE_FX_UNDERLYING) {
            fxr = underlyingStopLoss;
        } else if (getFxMode() == MODE_FX_RATE) {
            fxr = fxrStop;
        }

        return fxr;
    }

    /**
     * @param fxrStop the fxrStop to set
     */
    public void setFxrStop(Float fxrStop) {
        this.fxrStop = fxrStop;
    }

    /**
     * @return the fxrStopEntry
     */
    public Float getFxrStopEntry() {
        Float fxr = 1f;
        if (getFxMode() == MODE_FX_UNDERLYING) {
            fxr = underlyingStopEntry;
        } else if (getFxMode() == MODE_FX_RATE) {
            fxr = fxrStopEntry;
        }

        return fxr;
    }

    /**
     * @param fxrStopEntry the fxrStopEntry to set
     */
    public void setFxrStopEntry(Float fxrStopEntry) {
        this.fxrStopEntry = fxrStopEntry;
    }

    /**
     * @return the fxrEntry
     */
    public Float getFxrEntry() {
        Float fxr = 1f;
        if (getFxMode() == MODE_FX_UNDERLYING) {
            fxr = underlyingEntry;
        } else if (getFxMode() == MODE_FX_RATE) {
            fxr = fxrEntry;
        }

        return fxr;
    }

    /**
     * @param fxrEntry the fxrEntry to set
     */
    public void setFxrEntry(Float fxrEntry) {
        this.fxrEntry = fxrEntry;
    }

    /**
     * @return the fxrTarget
     */
    public Float getFxrTarget() {
        Float fxr = 1f;
        if (getFxMode() == MODE_FX_UNDERLYING) {
            fxr = underlyingTarget;
        } else if (getFxMode() == MODE_FX_RATE) {
            fxr = fxrTarget;
        }

        return fxr;
    }

    /**
     * @param fxrTarget the fxrTarget to set
     */
    public void setFxrTarget(Float fxrTarget) {
        this.fxrTarget = fxrTarget;
    }

    public String getPositionStatus() {
        String status = quantity > 0f ? POSITION_STATUS_OPEN : POSITION_STATUS_CLOSED;
        Iterator<Transaction> iterator = transactionHashMap.values().iterator();
        while (iterator.hasNext() && status.equals(POSITION_STATUS_CLOSED)) {
            if (iterator.next().isOpen()) {
                status = POSITION_STATUS_OPEN;
            }
        }

        return status;
    }

    public boolean isOpen() {
        return getPositionStatus().equals(POSITION_STATUS_OPEN);
    }

    public boolean isClosed() {
        return getPositionStatus().equals(POSITION_STATUS_CLOSED);
    }

    //@todo: set FxMode only once, when instrument is set
    private int getFxMode() {
        int fxMode = Position.MODE_FX_NONE;
        if (instrument.id_instrument_currency == instrument.id_instrument) {
            fxMode = Position.MODE_FX_UNDERLYING;
        } else if (instrument.id_instrument_currency != 0L) {
            fxMode = Position.MODE_FX_RATE;
        }

        return fxMode;
    }

    public boolean updateTransaction(String id_transacion) {
        boolean updateTransaction = false;
        if (transactionHashMap.containsKey(id_transacion)) {
            Transaction transaction = transactionHashMap.get(id_transacion);

            if (transaction.isOpen()) {
                if (transaction.isLimited()) {
                    updateTransaction = (transaction.isPurchase() && (marketPrice<=getEntry() || marketPrice <= transaction.price)) || (transaction.isSale() && (marketPrice>=getTarget() || marketPrice >= transaction.price));
                } else if (transaction.isStop()) {
                    //updateTransaction = (transaction.isPurchase() && (marketPrice>=getStopEntry() || marketPrice >= transaction.price)) || (transaction.isSale() && (marketPrice<=getStopLoss()));
                    updateTransaction =  (transaction.isSale() && (marketPrice<=getStopLoss()));
                } else if (transaction.isUnlimited()) {
                    updateTransaction = true;
                }
            }
        }
        Date d=new Date();
        updateTransaction &= (lastUpdate==null) || (lastUpdate.getTime()-d.getTime())>15000L;

        if (updateTransaction) lastUpdate=d;

        return updateTransaction;
    }

    public boolean isLong() { return id_position_type.equalsIgnoreCase(POSITION_TYPE_CALL); }
    public boolean isCall() { return isLong(); }

    public boolean isShort() { return id_position_type.equalsIgnoreCase(POSITION_TYPE_PUT); }
    public boolean isPut() { return isShort(); }
}



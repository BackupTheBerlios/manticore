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
package com.manticore.position;

import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;
import com.manticore.connection.Flatex;
import com.manticore.foundation.PositionDataStorage;
import com.manticore.foundation.TanReader;
import com.manticore.foundation.Transaction;
import com.manticore.stream.ArivaQuoteStream;
import com.manticore.foundation.WaveXXL;
import com.manticore.util.Settings;
import com.manticore.xmarkets.WaveXXLParser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PositionControler implements ChangeListener, ActionListener {
    //default settings for German Dax 30 Performance Index

    public final static int ORDER_MODE_DIRECT = 0;
    public final static int ORDER_MODE_LIMIT = 1;
    public final static int ORDER_MODE_LIMIT_STOP = 2;
    public final static int ORDER_MODE_NOLIMIT = 3;
    public final static int TAKE_PROFIT_MANUAL = 0;
    public final static int TAKE_PROFIT_AUTOMATIC = 1;
    public final static int TAKE_PROFIT_TRAILING = 2;
    public final static int TAKE_PROFIT_TRAILING_AUTOMATIC = 4;
    private int orderMode = ORDER_MODE_LIMIT;
    private int takeProfitMode = TAKE_PROFIT_MANUAL;
    private Float maximumMarketPrice = null;
    private Float trailingSLRatio = null;
    private long id_account = 1;
    private PositionView positionView;
    private Position position = null;
    String orderID = "";
    String orderID_SL = "";
    private ArivaQuoteStream underlyingStream = null;
    private ArivaQuoteStream securityStream = null;
    private TanReader tanReader;
    private PositionDataStorage positionDataStorage;
    private ChangeListener changeListener = null;

    public PositionControler(PositionDataStorage positionDataStorage, TanReader tanReader, Instrument instrument) {
        trailingSLRatio = Settings.getInstance().getFloat("manticore-trader", "PositionController", "trailingStopLoss");

        this.positionDataStorage = positionDataStorage;
        position = new Position(id_account, positionDataStorage.getNextPositionID("trader"), instrument);

        this.positionView = new PositionView(this);
        this.positionView.setOrderMode(orderMode);
        this.positionView.setTakeProfitMode(takeProfitMode);
        this.tanReader = tanReader;
    }

    public PositionControler(PositionDataStorage positionDataStorage, TanReader tanReader, Instrument instrument, Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        trailingSLRatio = Settings.getInstance().getFloat("manticore-trader", "PositionController", "trailingStopLoss");

        this.positionDataStorage = positionDataStorage;
        position = new Position(id_account, positionDataStorage.getNextPositionID("trader"), instrument);
        position.setUnderlyingStopLoss(stopLoss);
        position.setUnderlyingEntry(entry);
        position.setUnderlyingStopEntry(stopBuy);
        position.setUnderlyingTarget(takeProfit);
        position.setWaveXXL(WaveXXLParser.getWaveXXLByIsin(isin));

        positionView = new PositionView(this);
        positionView.setOrderMode(ORDER_MODE_LIMIT_STOP);
        positionView.setTakeProfitMode(TAKE_PROFIT_TRAILING);
        positionView.setPosition(position);
        positionView.resetRefreshButton();

        this.tanReader = tanReader;

        restartStream();
    }

    public PositionControler(PositionDataStorage positionDataStorage, TanReader tanReader, Position position) {
        trailingSLRatio = Settings.getInstance().getFloat("manticore-trader", "PositionController", "trailingStopLoss");

        this.positionDataStorage = positionDataStorage;
        this.position = position;

        this.position.setWaveXXL(WaveXXLParser.getWaveXXLByIsin(position.isin));

        this.positionView = new PositionView(this);
        this.positionView.setOrderMode(orderMode);
        this.positionView.setTakeProfitMode(takeProfitMode);
        positionView.setPosition(position);

        restartStream();

        Iterator<Transaction> iterator = position.getTransactionHashMap().values().iterator();
        while (iterator.hasNext()) {
            Transaction transaction = iterator.next();
            if (transaction.isOpen()) {
                if (transaction.isPurchase()) {
                    orderID = transaction.id_transaction;
                    updateOrderStatus();
                } else if (transaction.isSale()) {
                    orderID_SL = transaction.id_transaction;
                    updateSLOrderStatus();
                }
            }
        }

        this.tanReader = tanReader;
    }

    public ArrayList<String> getData() {
        ArrayList<String> ArrayList = new ArrayList();
        if (position.getWaveXXL() != null) {
            ArrayList.add(position.instrument.getSymbol());
            ArrayList.add(position.id_position_type);
            ArrayList.add(DecimalFormat.getInstance().format(position.getWaveXXL().getLeverage()));
            ArrayList.add(DecimalFormat.getIntegerInstance().format(position.quantity));
            ArrayList.add(DecimalFormat.getInstance().format(position.getMarketPrice()));
            ArrayList.add(DecimalFormat.getInstance().format(position.getPositionProfit()));
            ArrayList.add(position.getWaveXXL().getWkn());
        }
        return ArrayList;
    }

    public void setOrderMode() {
        if (orderMode == ORDER_MODE_DIRECT) {
            orderMode = ORDER_MODE_LIMIT;
        } else if (orderMode == ORDER_MODE_LIMIT) {
            orderMode = ORDER_MODE_LIMIT_STOP;
        } else if (orderMode == ORDER_MODE_LIMIT_STOP) {
            orderMode = ORDER_MODE_NOLIMIT;
        } else if (orderMode == ORDER_MODE_NOLIMIT) {
            orderMode = ORDER_MODE_DIRECT;
        }

        positionView.setOrderMode(orderMode);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(securityStream)) {
            Float marketPrice = securityStream.getCurrentPrice();

            position.setMarketPrice(marketPrice);

            if (position.updateTransaction(orderID)) {
                updateOrderStatus();
            } else if (position.updateTransaction(orderID_SL)) {
                updateSLOrderStatus();
            }

            adjustStopLoss(marketPrice);

            positionView.setPosition(position);
        }
    }

    public void adjustStopLoss(Float marketPrice) {
        if (position.quantity > 0) {
            if (takeProfitMode == TAKE_PROFIT_AUTOMATIC || takeProfitMode == TAKE_PROFIT_TRAILING_AUTOMATIC) {
                if (marketPrice > position.getTarget()) {
                    Logger.getAnonymousLogger().info("take profit automatically");
                    sellLimit();
                }
            } else if (takeProfitMode == TAKE_PROFIT_TRAILING || takeProfitMode == TAKE_PROFIT_TRAILING_AUTOMATIC) {
                if (maximumMarketPrice == null) {
                    maximumMarketPrice = marketPrice;
                } else if (maximumMarketPrice < marketPrice) {
                    maximumMarketPrice = marketPrice;

                    Float trailingSL = maximumMarketPrice * (1F + trailingSLRatio);
                    Logger.getAnonymousLogger().info("calculate trailing SL as of " + trailingSL);

                    if (trailingSL > position.getStopLoss()) {
                        position.setStopLoss(trailingSL);
                        Logger.getAnonymousLogger().info("set SL to " + position.getStopLoss());
                        stopLoss();
                    }
                }
            }
        } else {
            maximumMarketPrice = null;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equalsIgnoreCase("refresh")) {

            if (position.getWaveXXL() == null || (position.getQuantity() == 0 && orderID.length() == 0)) {
                try {
                    position = positionView.getPosition(position);
                    new FindWaveXXLThread().run();
                } catch (ParseException ex) {
                    Logger.getLogger(PositionControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    stopStream();
                    position = positionView.getPosition(position);
                    positionView.setPosition(position);
                    restartStream();
                } catch (ParseException ex) {
                    Logger.getLogger(PositionControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } else if (e.getActionCommand().equalsIgnoreCase("sell")) {
            if (orderMode == ORDER_MODE_LIMIT) {
                sellLimit();
            } else if (orderMode == ORDER_MODE_DIRECT) {
                sellDirect();
            } else if (orderMode == ORDER_MODE_LIMIT_STOP) {
                sellLimitStop();
            } else if (orderMode == ORDER_MODE_NOLIMIT) {
                sellNoLimit();
            }

        } else if (e.getActionCommand().equalsIgnoreCase("buy")) {
            if (orderMode == ORDER_MODE_LIMIT) {
                buyLimit();
            } else if (orderMode == ORDER_MODE_DIRECT) {
                buyDirect();
            } else if (orderMode == ORDER_MODE_LIMIT_STOP) {
                buyLimitStop();
            } else if (orderMode == ORDER_MODE_NOLIMIT) {
                buyNoLimit();
            }

        } else if (e.getActionCommand().equalsIgnoreCase("stopLoss")) {
            stopLoss();

        } else if (e.getActionCommand().equalsIgnoreCase("stopLossEntry")) {
            stopLossEntry();

        } else if (e.getActionCommand().equalsIgnoreCase("cancelOrder")) {
            cancelOrder(orderID);

        } else if (e.getActionCommand().equalsIgnoreCase("updateOrderStatus")) {
            updateOrderStatus();
        } else if (e.getActionCommand().equalsIgnoreCase("updateSLOrderStatus")) {
            updateSLOrderStatus();
        } else if (e.getActionCommand().equalsIgnoreCase("orderMode")) {
            setOrderMode();

            //update Stop-Entry as field is not always available
            //@todo: find a better way do synchronize this --> update, when field is available???
            if (position != null) {
                try {
                    position = positionView.getPosition(position);
                    positionView.setPosition(position);
                } catch (ParseException ex) {
                    Logger.getLogger(PositionControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else if (e.getActionCommand().equalsIgnoreCase("TAKE_PROFIT_MODE")) {
            setTakeProfitMode();
        }


    }

    public void buyLimit() {
        if (Flatex.lock()) {
            Flatex connection = Flatex.getInstance(tanReader);
            connection.searchPaper(position.getWaveXXL().getWkn());
            orderID = connection.orderLimit(Flatex.ORDER_BUY, position.getAvailableShares(), Flatex.LIMIT_LIMIT, position.getEntry(), null, Flatex.EXTENSION_NONE);
            Flatex.unlock();

            savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, new Date(), position.getEntry(), 0f, position.getAvailableShares()));
            positionView.setOrder("buy " + orderID);
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }
        positionView.resetBuyButton();
    }

    public void buyDirect() {
        if (Flatex.lock()) {
            Flatex connection = Flatex.getInstance(tanReader);
            connection.searchPaper(position.getWaveXXL().getWkn());
            orderID = connection.orderDirect(position.id_position, Flatex.ORDER_BUY, position.getAvailableShares(), position.getEntry(), position.getStopLoss());
            Flatex.unlock();

            if (orderID.length() > 0) {
                savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_DIRECT, new Date(), position.getMarketPrice(), 0f, position.getAvailableShares()));
                positionView.setOrder("buy " + orderID);
            }
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }
        positionView.resetBuyButton();
    }

    public void sellDirect() {
        if (Flatex.lock()) {
            if (cancelSL()) {
                Flatex connection = Flatex.getInstance(tanReader);
                connection.searchPaper(position.getWaveXXL().getWkn());
                orderID = connection.orderDirect(position.id_position, Flatex.ORDER_SELL, position.getQuantity(), null, null);

                if (orderID.length() > 0) {
                    savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_DIRECT, new Date(), position.getMarketPrice(), 0f, -position.getQuantity()));
                    positionView.setOrder("sell " + orderID);
                }
            }
            Flatex.unlock();
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetSellButton();
    }

    public void sellLimit() {
        if (Flatex.lock()) {
            if (cancelSL()) {
                Flatex connection = Flatex.getInstance(tanReader);
                connection.searchPaper(position.getWaveXXL().getWkn());
                orderID = connection.orderLimit(Flatex.ORDER_SELL, position.getQuantity(), Flatex.LIMIT_LIMIT, position.getTarget(), null, Flatex.EXTENSION_NONE);

                savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, new Date(), position.getTarget(), 0f, -position.getAvailableShares()));
                positionView.setOrder("sell " + orderID);
            }
            Flatex.unlock();
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetSellButton();
    }

    private void cancelOrder(String id_transaction) {
        if (Flatex.lock()) {
            Transaction transaction = Flatex.getInstance(tanReader).getTransaction(position.id_position, id_transaction);
            Flatex.unlock();

            if (transaction != null && transaction.isOpen()) {
                Flatex.getInstance(tanReader).cancelOrder(id_transaction);
                savePosition(transaction);
                positionView.setOrder("cancel " + id_transaction);
            }
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }
        positionView.resetCancelButton();
    }

    public void cancelOrders() {
        Iterator<Transaction> iterator = position.getTransactionHashMap().values().iterator();
        while (iterator.hasNext()) {
            Transaction transaction=iterator.next();
            if (transaction.isOpen() && transaction.isPurchase()) {
                cancelOrder(transaction.id_transaction);
            }
        }
    }

    /**
     * @return the positionView
     */
    public PositionView getPositionView() {
        return positionView;
    }

    /**
     * @param positionView the positionView to set
     */
    public void setPositionView(PositionView positionView) {
        this.positionView = positionView;
    }

    /**
     * @return the underlyingDescription
     */
    public String getUnderlyingDescription() {
        return position.instrument.getName();
    }

    public void updateOrderStatus() {
        Logger.getLogger(this.getClass().getName()).fine("update order status " + orderID);
        new UpdateOrderThread(orderID, 0).start();
    }

    public void updateSLOrderStatus() {
        Logger.getLogger(this.getClass().getName()).fine("update SL order status " + orderID_SL);
        new UpdateOrderThread(orderID_SL, 1).start();
    }

    public void updateStatus() {
        Iterator<Transaction> iterator = position.getTransactionHashMap().values().iterator();
        while (iterator.hasNext()) {
            Transaction transaction=iterator.next();
            if (transaction.isOpen()) {
                if (transaction.id_transaction.equalsIgnoreCase(orderID)) updateOrderStatus();
                else updateSLOrderStatus();
            }
        }
    }

    //do NOT insert a lock here, as method always will be executed inside a lock!
    public boolean cancelSL() {
        boolean canceled = false;
        if (orderID_SL != null && orderID_SL.length() > 0) {
            Flatex.getInstance(tanReader).cancelOrder(orderID_SL);
            Transaction transaction = Flatex.getInstance(getTanReader()).getTransaction(position.id_position, orderID);
            if (transaction.isCanceled()) {
                savePosition(transaction);
                orderID_SL = "";
                canceled = true;
            }
        }
        return canceled;
    }

    public void sellLimitStop() {
        if (Flatex.lock()) {
            if (cancelSL()) {
                Flatex connection = Flatex.getInstance(tanReader);
                connection.searchPaper(position.getWaveXXL().getWkn());
                orderID = connection.orderLimit(Flatex.ORDER_SELL, position.getQuantity(), Flatex.LIMIT_STOP_LIMIT, position.getTarget(), position.getTarget(), Flatex.EXTENSION_NONE);
                positionView.setOrder("sell " + orderID);
                savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_STOP_LIMITED, new Date(), position.getTarget(), 0f, -position.getQuantity()));
            }
            Flatex.unlock();
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetSellButton();
    }

    public void buyLimitStop() {
        if (Flatex.lock()) {
            Flatex connection = Flatex.getInstance(tanReader);
            connection.searchPaper(position.getWaveXXL().getWkn());
            orderID = connection.orderLimit(Flatex.ORDER_BUY, position.getAvailableShares(), Flatex.LIMIT_STOP_LIMIT, position.getEntry(), position.getStopEntry(), Flatex.EXTENSION_NONE);
            Flatex.unlock();

            savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_STOP_LIMITED, new Date(), position.getEntry(), 0f, position.getAvailableShares()));
            positionView.setOrder("buy " + orderID);
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetBuyButton();
    }

    public void stopLoss() {
        if (Flatex.lock()) {
            if (orderID_SL.length() == 0 || (position.getTransactionHashMap().containsKey(orderID_SL) && position.getTransactionHashMap().get(orderID_SL).isCanceled())) {
                Flatex connection = Flatex.getInstance(tanReader);
                connection.searchPaper(position.getWaveXXL().getWkn());
                orderID_SL = connection.orderLimit(Flatex.ORDER_SELL, position.getQuantity(), Flatex.LIMIT_STOP_MARKET, null, position.getStopLoss(), Flatex.EXTENSION_NONE);
                Flatex.unlock();

                savePosition(new Transaction(position.id_position, orderID_SL, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_STOP, new Date(), position.getStopLoss(), 0f, -position.getQuantity()));
                positionView.setSLOrder("new Stopp Loss " + orderID_SL);
            } else {
                Flatex connection = Flatex.getInstance(tanReader);
                connection.adjustSLOrder(orderID_SL, position.getStopLoss(), position.getQuantity().intValue());
                Flatex.unlock();

                savePosition(new Transaction(position.id_position, orderID_SL, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_STOP, new Date(), position.getStopLoss(), 0f, -position.getQuantity()));
                positionView.setSLOrder("adjusted Stopp Loss " + orderID_SL);
            }
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetStopLossButton();
        positionView.resetStopLossEntryButton();
    }

    private void stopStream() {
        if (securityStream != null) {
            securityStream.removeChangeListener(this);
            if (changeListener != null) {
                securityStream.removeChangeListener(changeListener);
            }
            securityStream.stopThread();
        }
    }

    private void restartStream() {
        stopStream();

        //@todo: fix the hardcoded stream identifier
        securityStream = new ArivaQuoteStream(position.getWaveXXL().getWkn(), "@31.6");
        securityStream.addChangeListener(this);
        if (changeListener != null) {
            securityStream.addChangeListener(changeListener);
        }
    }

    public void addStreamChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
        if (securityStream != null) {
            securityStream.addChangeListener(changeListener);
        }
    }

    public void sellNoLimit() {
        if (Flatex.lock()) {
            if (cancelSL()) {

                Flatex connection = Flatex.getInstance(tanReader);
                connection.searchPaper(position.getWaveXXL().getWkn());
                orderID = connection.orderLimit(Flatex.ORDER_SELL, position.getQuantity(), Flatex.LIMIT_NONE, null, null, Flatex.EXTENSION_FOK);
                Flatex.unlock();

                savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_UNLIMITED, new Date(), position.getMarketPrice(), 0f, -position.getQuantity()));
                positionView.setOrder("sell unlimited " + orderID);
            }
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }

        positionView.resetSellButton();
    }

    public void buyNoLimit() {
        if (Flatex.lock()) {
            orderID = "";

            Flatex connection = Flatex.getInstance(tanReader);
            connection.searchPaper(position.getWaveXXL().getWkn());
            orderID = connection.orderLimit(Flatex.ORDER_BUY, position.getAvailableShares(), Flatex.LIMIT_NONE, null, null, Flatex.EXTENSION_FOK);
            Flatex.unlock();

            savePosition(new Transaction(position.id_position, orderID, Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_UNLIMITED, new Date(), position.getMarketPrice(), 0f, -position.getAvailableShares()));
            positionView.setOrder("buy unlimited" + orderID);
        } else {
            Logger.getLogger(getClass().getName()).warning("can not order while connection is locked");
        }
        positionView.resetBuyButton();
    }

    public boolean close() {
        boolean closed = false;

        if (position.isClosed()) {
            stopStream();
            positionView.dispose();
            positionView = null;
            position = null;
            closed = true;
        }
        return closed;
    }

    private void savePosition(Transaction transaction) {
        position.getTransactionHashMap().put(transaction.id_transaction, transaction);
        positionDataStorage.writePosition("trader", position);
        positionDataStorage.writeTransaction("trader", transaction);
    }

    /**
     * @return the tanReader
     */
    public TanReader getTanReader() {
        return tanReader;
    }

    /**
     * @param tanReader the tanReader to set
     */
    public void setTanReader(TanReader tanReader) {
        this.tanReader = tanReader;
    }

    public void setTakeProfitMode() {
        if (takeProfitMode == TAKE_PROFIT_MANUAL) {
            takeProfitMode = TAKE_PROFIT_TRAILING;
        } else if (takeProfitMode == TAKE_PROFIT_TRAILING) {
            takeProfitMode = TAKE_PROFIT_AUTOMATIC;
        } else if (takeProfitMode == TAKE_PROFIT_AUTOMATIC) {
            takeProfitMode = TAKE_PROFIT_TRAILING_AUTOMATIC;
        } else if (takeProfitMode == TAKE_PROFIT_TRAILING_AUTOMATIC) {
            takeProfitMode = TAKE_PROFIT_MANUAL;
        }
        positionView.setTakeProfitMode(takeProfitMode);
    }

    public void stopLossEntry() {
        if (position.getStopLoss() < position.averageEntry) {
            position.setStopLoss(position.averageEntry + position.getWaveXXL().getSpread());
        } else if (position.getStopLoss() > position.averageEntry) {
            position.setStopLoss(position.getMarketPrice() - position.getWaveXXL().getSpread());
        }

        Logger.getAnonymousLogger().log(Level.INFO, "set SL to {0}", position.getStopLoss());
        stopLoss();
    }

    private void findWaveXLL() {
        stopStream();

        Float underlyingStrike = position.getUnderlyingStrike();

        String inbwnr = position.instrument.getKey("3");
        WaveXXL waveXXL = WaveXXLParser.getWaveXXLByStrike(inbwnr, position.getMode(), underlyingStrike);
        position.setWaveXXL(waveXXL);

        positionView.setPosition(position);
        positionView.resetRefreshButton();

        restartStream();

        //open connection in order to save time for entering a position
        Flatex connection = Flatex.getInstance(tanReader);
    }

    /**
     * @return the position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    private class FindWaveXXLThread implements Runnable {

        @Override
        public void run() {
            findWaveXLL();
        }
    }

    private class UpdateOrderThread extends Thread {

        String orderID = "";
        int mode = 0;

        UpdateOrderThread(String orderID, int mode) {
            this.orderID = orderID;
            this.mode = mode;
        }

        @Override
        public void run() {
            if (mode == 0) {
                positionView.setOrder("update Status: " + orderID);
            } else if (mode == 1) {
                positionView.setSLOrder("update Status: " + orderID);
            }

            //wait for a free connection
            while (!Flatex.lock()) {
                Logger.getLogger(getClass().getName()).info("Connection locked, so wait for some seconds.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PositionControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            boolean changeBalance = (getPosition().getTransactionHashMap().containsKey(orderID) && getPosition().getTransactionHashMap().get(orderID).isOpen());

            Transaction transaction = Flatex.getInstance(getTanReader()).getTransaction(getPosition().id_position, orderID);
            Flatex.unlock();

            if (transaction != null && changeBalance) {
                if (transaction.isExecuted()) {
                    //Float executionPrice = Connection2.getInstance().getExecutionPrice(orderID);
                    if (transaction.isPurchase()) {
                        getPosition().buy(transaction.price, transaction.quantity);
                    } else if (transaction.isSale()) {
                        getPosition().sell(transaction.price, transaction.quantity);
                    }
                }

                // set new SL
                if (getPosition().quantity > 0L && ((transaction.isPurchase() && transaction.isExecuted())
                        || (transaction.isSale() && transaction.isCanceled()))) {

                    //delete orderid when SL was canceled so it can be issued again
                    if (mode == 1) {
                        orderID_SL = "";
                    }
                    stopLoss();
                }

                getPositionView().setPosition(getPosition());
            }

            savePosition(transaction);

            if (mode == 0) {
                positionView.setOrder(transaction.getDescription());
                positionView.resetUpdateOrderButton();

            } else if (mode == 1) {
                positionView.setSLOrder(transaction.getDescription());
                positionView.resetUpdateSLOrderButton();
            }

            positionView.resetRefreshButton();
        }
    }
}

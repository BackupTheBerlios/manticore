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
package com.manticore.system;

import com.manticore.chart.CandleArrayList;
import com.manticore.chart.ChartCanvas;
import com.manticore.chart.ChartParameters;
import com.manticore.chart.ExtremumArrayList;
import com.manticore.chart.PeriodSettings;
import com.manticore.connection.Flatex;
import com.manticore.database.Quotes;
import com.manticore.database.TickDataTimerTask;
import com.manticore.foundation.Candle;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;
import com.manticore.foundation.StockExchange;
import com.manticore.foundation.Tick;
import com.manticore.position.PositionControler;
import com.manticore.position.PositionGrid;
import com.manticore.stream.ArivaQuoteStream;
import com.manticore.stream.WatchDog;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;

public class TradingSystem implements ChangeListener {

    public static int CLOSING_MINUTES = 15;
    public static int DEACTIVATION_CANDLES = 3;
    public static String ISIN_LONG = "DB41RV";
    public static String ISIN_SHORT = "DB0M18";
    public static final int MODE_LONG = 1;
    public static final int MODE_NEUTRAL = 0;
    public static final int MODE_SHORT = -1;
    public static final int OPEN_MINUTES = 15;
    public static final Logger logger = Logger.getLogger(TradingSystem.class.getName());
    public ChartParameters chartParameters;
	 public ChartCanvas chartCanvas;
    public CandleArrayList candleArrayList;
    public DateTime lastUpdate;
	 public Tick lastTick;
	 public DateTime lastCandleEnd;
    public ArivaQuoteStream arivaQuoteStream;
    public TickDataTimerTask tickDataTimerTask;
    public PositionGrid positionGrid;
    public Flatex connection;
    public int mode = MODE_NEUTRAL;
    public long activeTimeStamp = 0;
    public Candle candle;
    public ExtremumArrayList extremumArrayList;

    public Float lastTmpLow;
    public Float previousTmpLow;
    public Float lastTmpHigh;
    public Float previousTmpHigh;

    public TradingSystem(PositionGrid positionGrid, ChartCanvas chartCanvas) {


        lastUpdate = new DateTime(DateTimeZone.forID("Europe/Berlin"));
		  lastTick=new Tick(lastUpdate, 0F, 0L);

        Instrument instrument = Quotes.getInstance().getInstrumentFromID(1);
        StockExchange stockExchange = Quotes.getInstance().getStockExchange(22);

        this.positionGrid = positionGrid;
        positionGrid.setInstrument(instrument);

        chartParameters = new ChartParameters(0, instrument, stockExchange, getDateTimeTo(stockExchange), getPeriodSettings());

		  this.chartCanvas=chartCanvas;
		  if (this.chartCanvas!=null) {
				chartCanvas.setChartParameters(chartParameters);
				chartCanvas.updateChartSettings();
				chartCanvas.drawChart();
		  }

        tickDataTimerTask = new TickDataTimerTask(chartParameters.getInstrument().getId(), chartParameters.getStockExchange().getId(), chartParameters.getStockExchange().getOpeningDate());
        tickDataTimerTask.addChangeListener(this);

        candleArrayList = new CandleArrayList(chartParameters);
        candle = candleArrayList.getLastValidCandle();

        updateArivaStreamQuote();
    }

    public TradingSystem() {
    }

    public static PeriodSettings getPeriodSettings() {
        return new PeriodSettings(DurationFieldType.days(), 5, DurationFieldType.days(), 1, DurationFieldType.hours(), 2, DurationFieldType.minutes(), 5);
    }

    public static DateTime getDateTimeTo(StockExchange stockExchange) {
        MutableDateTime dateTimeTo = new MutableDateTime();
        dateTimeTo.setMinuteOfDay(stockExchange.getClosingMinute());
        dateTimeTo.setSecondOfMinute(0);
        dateTimeTo.setMillisOfSecond(0);

        return dateTimeTo.toDateTime();
    }

    public boolean activateLong() {
        return mode == MODE_NEUTRAL
                && lastTmpLow > previousTmpLow;
    }

    public boolean activateShort() {
        return mode == MODE_NEUTRAL
                && lastTmpHigh < previousTmpHigh;
    }

    public void buyDirect(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        if (isin.length() > 0 && lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            PositionControler positionControler = positionGrid.addPositionControler(stopLoss, entry, stopBuy, takeProfit, isin);
            if (positionControler != null) {
                Position position = positionControler.getPosition();
                logger.log(Level.INFO, "{0} buy {1} shares at entry={3}, SL={4}", new Object[]{position.getIsin(), position.getAvailableShares(), position.getAmount(), position.getEntry(), position.getStopLoss()});
                positionControler.buyDirect();
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void buyLimit(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        if (isin.length() > 0 && lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            PositionControler positionControler = positionGrid.addPositionControler(stopLoss, entry, stopBuy, takeProfit, isin);
            if (positionControler != null) {
                Position position = positionControler.getPosition();
                logger.log(Level.INFO, "{0} buy {1} shares at entry={3}, SL={4}", new Object[]{position.getIsin(), position.getAvailableShares(), position.getAmount(), position.getEntry(), position.getStopLoss()});
                positionControler.buyLimit();
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void buyLimitStop(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        if (isin.length() > 0 && lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            PositionControler positionControler = positionGrid.addPositionControler(stopLoss, entry, stopBuy, takeProfit, isin);
            if (positionControler != null) {
                Position position = positionControler.getPosition();
                logger.log(Level.INFO, "{0} buy {1} shares at entry={3}, SL={4}", new Object[]{position.getIsin(), position.getAvailableShares(), position.getAmount(), position.getEntry(), position.getStopLoss()});
                positionControler.buyLimitStop();
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void buyLong() {
        Float entry = lastTmpHigh;
        Float stopBuy = lastTmpHigh;
        Float takeProfit = lastTmpHigh + 50;
        Float stopLoss = Math.max(lastTmpLow, entry - 10);

        buyLimit(stopLoss, entry, stopBuy, takeProfit, getIsinLong());

        logger.log(Level.INFO, "deactivate long after placing order");
        mode = MODE_NEUTRAL;
    }

    public void buyNoLimit(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
        if (isin.length() > 0 && lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            PositionControler positionControler = positionGrid.addPositionControler(stopLoss, entry, stopBuy, takeProfit, isin);
            if (positionControler != null) {
                Position position = positionControler.getPosition();
                logger.log(Level.INFO, "{0} buy {1} shares at entry={3}, SL={4}", new Object[]{position.getIsin(), position.getAvailableShares(), position.getAmount(), position.getEntry(), position.getStopLoss()});
                positionControler.buyNoLimit();
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void buyShort() {
        Float entry = lastTmpLow;
        Float stopLoss = Math.min(lastTmpHigh, entry + 10);
        Float stopBuy = lastTmpLow;
        Float takeProfit = lastTmpLow - 50;
        buyLimit(stopLoss, entry, stopBuy, takeProfit, getIsinLong());

        logger.log(Level.INFO, "deactivate short after placing order");
        mode = MODE_NEUTRAL;
    }

    public void cancel(int mode) {
        if (lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            Iterator<PositionControler> positionControlerIterator = positionGrid.getPositionControlerArrayList().iterator();
            while (positionControlerIterator.hasNext()) {
                PositionControler positionControler = positionControlerIterator.next();
                if ((positionControler.getPosition().isLong() && mode == MODE_LONG) || (positionControler.getPosition().isShort() && mode == MODE_SHORT)) {

                    positionControler.updateOrderStatus();
                    positionControler.cancelOrders();
                }
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void adjustStopLoss(int mode, Float stopLoss) {
        if (lastUpdate.isBefore(candle.getEnd())) {
            //open connection in order to save time for entering a position
            connection = Flatex.getInstance(Quotes.getInstance());

            Iterator<PositionControler> positionControlerIterator = positionGrid.getPositionControlerArrayList().iterator();
            while (positionControlerIterator.hasNext()) {
                PositionControler positionControler = positionControlerIterator.next();
                if ((positionControler.getPosition().isLong() && mode == MODE_LONG) || (positionControler.getPosition().isShort() && mode == MODE_SHORT)) {

                    positionControler.updateOrderStatus();
                    positionControler.adjustStopLoss(stopLoss);
                }
            }
            lastUpdate = candle.getEnd();
        }
    }

    public void cancelShort() {
        cancel(MODE_SHORT);
    }

    public void cancelLong() {
        cancel(MODE_LONG);
    }

    public void sellEOD() {
        if (positionGrid.hasOpenPosition()) {
            logger.log(Level.INFO, "close open positions on end of day");
            Iterator<PositionControler> iterator = positionGrid.getPositionControlerArrayList().iterator();
            while (iterator.hasNext()) {
                PositionControler positionControler = iterator.next();
                positionControler.updateOrderStatus();
                if (shallClosePositionEoD(positionControler.getPosition()) && positionControler.getPosition().quantity > 0) {
                    sellOnEOD(positionControler);
                }
            }
        }
    }

    public boolean shallSellOnStoppingSystem(Position position) {
        return true;
    }

    public boolean shallClosePositionEoD(Position position) {
        return true;
    }

    public boolean deactivateLong() {
        return candle.getLow() < lastTmpLow;
    }

    public boolean deactivateShort() {
        return candle.getHigh() > lastTmpHigh;
    }

    public boolean enterLong() {
        return mode == MODE_LONG
                && candle.getClosing() > lastTmpHigh;
    }

    public boolean enterShort() {
        return mode == MODE_SHORT
                && candle.getClosing() < lastTmpLow;
    }

    public boolean exitLong() {
        return candle.getLow() < lastTmpLow;
    }

    public boolean exitShort() {
        return candle.getHigh() > lastTmpHigh;
    }

    public void findSignals() {
        Object[] object = new Object[]{
            lastTick.getDateTime(), candle.getClosing(), lastTmpLow, previousTmpLow, lastTmpHigh, previousTmpHigh
        };

        //if (isValidCandle() && chartParameters.getStockExchange().open()) {
		  if (isValidCandle()) {
            //logger.log(Level.INFO, "found candle with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);

            if (activateLong()) {
                mode = MODE_LONG;
                activeTimeStamp = candle.getStart().getMillis();
                logger.log(Level.INFO, "{0}: activate long with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
            }
            if (activateShort()) {
                mode = MODE_SHORT;
                activeTimeStamp = candle.getStart().getMillis();
                logger.log(Level.INFO, "{0}: activate short with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
            }
            if (mode == MODE_LONG && enterLong()) {
                logger.log(Level.INFO, "{0}: enter long with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
                buyLong();

                logger.log(Level.INFO, "deactivate long after placing order");
                mode = MODE_NEUTRAL;
            }
            if (mode == MODE_SHORT && enterShort()) {
                logger.log(Level.INFO, "{0}: enter short with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
                buyShort();

                logger.log(Level.INFO, "deactivate short after placing order");
                mode = MODE_NEUTRAL;
            }
            if (mode == MODE_LONG && deactivateLong()) {
                logger.log(Level.INFO, "{0}: cancel long with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);

                cancelLong();
                mode = MODE_NEUTRAL;
            }
            if (exitLong()) {
                logger.log(Level.INFO, "{0}: exit long with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);

                sellLongOnExit();
            }

            if (shouldAdjustStopLossLong()) {
                logger.log(Level.INFO, "{0}: adjust stop loss long with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
                adjustStopLossLong();
            }

            if (mode == MODE_SHORT && deactivateShort()) {
                logger.log(Level.INFO, "{0}: cancel short with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);

                cancelShort();
                mode = MODE_NEUTRAL;
            }

            // ugly hack in order to work around positionGrid for simulations
            //@todo: replace grid by collection of positions
            if (exitShort() && positionGrid!=null && positionGrid.hasOpenShortPosition()) {
                logger.log(Level.INFO, "{0}: exit short with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
                sellShortOnExit();
            }

            if (shouldAdjustStopLossShort()) {
                logger.log(Level.INFO, "{0}: adjust stop loss short with closing={0}, last low={1}, previous low={2}, last high={3}, previous high={4},", object);
                adjustStopLossShort();
            }
        }

        if (chartParameters.getStockExchange().closesWithinMinutes(CLOSING_MINUTES)) {
            sellEOD();
            mode = MODE_NEUTRAL;
        }
    }

    public String getIsinLong() {
        return ISIN_LONG;
    }

    public String getIsinShort() {
        return ISIN_SHORT;
    }

    public boolean isValidCandle() {
        int candlePosition = candle.getPosition();
        Candle prevCandle = candleArrayList.get(candlePosition - 1);
        boolean valid = !candle.isFirst() && lastUpdate.isBefore(candle.getEnd()) && candle.getStochasticRSI_SlowK() != null && prevCandle.getStochasticRSI_SlowK() != null;
        return valid;
    }

    public void sellDirect(int mode, Float stopLoss, Float takeProfit) {
        connection = Flatex.getInstance(Quotes.getInstance());

        if (lastUpdate.isBefore(candle.getEnd())) {
            lastUpdate = candle.getEnd();
            Iterator<PositionControler> positionControlerIterator = positionGrid.getPositionControlerArrayList().iterator();
            while (positionControlerIterator.hasNext()) {
                PositionControler positionControler = positionControlerIterator.next();
                if ((positionControler.getPosition().isLong() && mode == MODE_LONG) || (positionControler.getPosition().isShort() && mode == MODE_SHORT)) {
                    positionControler.updateOrderStatus();

                    if (positionControler.getPosition().quantity > 0) {
                        positionControler.getPosition().setUnderlyingStopLoss(stopLoss);
                        positionControler.getPosition().setUnderlyingTarget(takeProfit);
                        if (positionControler.cancelSL()) {
                            positionControler.sellDirect();
                        }
                    }
                }
            }
        }
    }

    public void sellLimit(int mode, Float stopLoss, Float takeProfit) {
        connection = Flatex.getInstance(Quotes.getInstance());

        if (lastUpdate.isBefore(candle.getEnd())) {
            lastUpdate = candle.getEnd();
            Iterator<PositionControler> positionControlerIterator = positionGrid.getPositionControlerArrayList().iterator();
            while (positionControlerIterator.hasNext()) {
                PositionControler positionControler = positionControlerIterator.next();
                if ((positionControler.getPosition().isLong() && mode == MODE_LONG) || (positionControler.getPosition().isShort() && mode == MODE_SHORT)) {
                    positionControler.updateOrderStatus();

                    if (positionControler.getPosition().quantity > 0) {
                        positionControler.getPosition().setUnderlyingStopLoss(stopLoss);
                        positionControler.getPosition().setUnderlyingTarget(takeProfit);
                        if (positionControler.cancelSL()) {
                            positionControler.sellLimit();
                        }
                    }
                }
            }
        }
    }

    public void sellLongOnExit() {
        sellDirect(MODE_LONG, lastTmpLow, lastTmpHigh);
    }

    public void sellNoLimit(int mode, Float stopLoss, Float takeProfit) {
        connection = Flatex.getInstance(Quotes.getInstance());

        if (lastUpdate.isBefore(candle.getEnd())) {
            lastUpdate = candle.getEnd();
            Iterator<PositionControler> positionControlerIterator = positionGrid.getPositionControlerArrayList().iterator();
            while (positionControlerIterator.hasNext()) {
                PositionControler positionControler = positionControlerIterator.next();
                if ((positionControler.getPosition().isLong() && mode == MODE_LONG) || (positionControler.getPosition().isShort() && mode == MODE_SHORT)) {
                    positionControler.updateOrderStatus();

                    if (positionControler.getPosition().quantity > 0) {
                        positionControler.getPosition().setUnderlyingStopLoss(stopLoss);
                        positionControler.getPosition().setUnderlyingTarget(takeProfit);
                        if (positionControler.cancelSL()) {
                            positionControler.sellNoLimit();
                        }
                    }
                }
            }
        }
    }

    public void sellOnStoppingSystem(PositionControler positionControler) {
        if (positionControler.cancelSL()) {
            positionControler.sellDirect();
        }
    }

    public void sellOnEOD(PositionControler positionControler) {
        if (positionControler.cancelSL()) {
            positionControler.sellDirect();
        }
    }

    public void sellShortOnExit() {
        sellDirect(MODE_SHORT, lastTmpHigh, lastTmpLow);
    }

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() instanceof ArivaQuoteStream) {
            ArivaQuoteStream ariveSreamQuote = (ArivaQuoteStream) e.getSource();
            lastTick=ariveSreamQuote.getLastTick();
            
				candle = candleArrayList.getCandleFromTick(lastTick);

				FindSignalThread findSignalThread = new FindSignalThread();
            findSignalThread.start();
        } else if (e.getSource().equals(tickDataTimerTask)) {
            logger.finest("rerun trade system now");
            DateTime dateTime=new DateTime(DateTimeZone.forID("Europe/Berlin"));

            candleArrayList = new CandleArrayList(chartParameters);
            extremumArrayList=candleArrayList.getExtremumArrayList();

            lastTmpHigh=extremumArrayList.getLastTmpHigh(dateTime).getPrice();
            previousTmpHigh=extremumArrayList.getPreviousTmpHigh(dateTime).getPrice();

            lastTmpLow=extremumArrayList.getLastTmpLow(dateTime).getPrice();
            previousTmpLow=extremumArrayList.getPreviousTmpLow(dateTime).getPrice();

            candle = candleArrayList.getCandle(dateTime);
            //candle = candleArrayList.getLastValidCandle();
            FindSignalThread findSignalThread = new FindSignalThread();
            findSignalThread.start();
        }
    }

    public void stopSystem() {
        tickDataTimerTask.removeChangeListener(this);
        tickDataTimerTask.cancel();
        tickDataTimerTask = null;

        if (positionGrid.hasOpenPosition()) {
            logger.log(Level.INFO, "close open positions before program closes");
            Iterator<PositionControler> iterator = positionGrid.getPositionControlerArrayList().iterator();
            while (iterator.hasNext()) {
                PositionControler positionControler = iterator.next();
                positionControler.updateOrderStatus();
                if (shallSellOnStoppingSystem(positionControler.getPosition()) && positionControler.getPosition().quantity > 0) {
                    sellOnStoppingSystem(positionControler);
                }
            }
            mode = MODE_NEUTRAL;
        }
        connection.close();
    }

    private void updateArivaStreamQuote() {
        WatchDog.getInstance().removeChangeListener(this);

        if (arivaQuoteStream != null) {
            arivaQuoteStream.removeChangeListener(this);
            arivaQuoteStream.stopThread();
        }

        int id_ext_key = 1;
        long id_instrument = chartParameters.getInstrument().getId();
        long id_stock_exchange = chartParameters.getStockExchange().getId();

        String key = Quotes.getInstance().getExtKeyInstrument(id_ext_key, id_instrument, id_stock_exchange);
        if (key.length() > 0) {
            arivaQuoteStream = new ArivaQuoteStream(key);
            arivaQuoteStream.addChangeListener(this);
        }
    }

    public boolean shouldAdjustStopLossLong() {
        return false;
    }

    public void adjustStopLossLong() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void adjustStopLossShort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean shouldAdjustStopLossShort() {
        return false;
    }

    private class FindSignalThread extends Thread {

        @Override
        public void run() {
            findSignals();
        }
    }
}

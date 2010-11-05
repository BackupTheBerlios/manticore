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

import com.manticore.chart.ChartCanvas;
import com.manticore.position.PositionGrid;
import java.util.logging.Level;

/**
 *
 * @author are
 */
public class StochRSIBreakOut extends TradingSystem {

    Float activeLongTrigger = 3f;
    Float activeShortTrigger = 97f;

    public StochRSIBreakOut(PositionGrid positionGrid, ChartCanvas chartCanvas) {
        super(positionGrid, chartCanvas);
    }

	 @Override
    public void buyLong() {
        Float entry = lastTmpHigh;
        Float stopBuy = lastTmpHigh;
        Float takeProfit = lastTmpHigh + 50;
        Float stopLoss = Math.max(lastTmpLow, entry - 10);
        buyLimit(stopLoss, entry, stopBuy, takeProfit, getIsinLong());

        logger.log(Level.INFO, "deactivate long after placing order");
        mode = MODE_NEUTRAL;
    }

	 @Override
    public void buyShort() {
        Float entry = lastTmpLow;
        Float stopLoss = Math.min(lastTmpHigh, entry + 10);
        Float stopBuy = lastTmpLow;
        Float takeProfit = lastTmpLow - 50;
        buyLimit(stopLoss, entry, stopBuy, takeProfit, getIsinLong());

		  logger.log(Level.INFO, "deactivate short after placing order");
        mode = MODE_NEUTRAL;
    }

	 @Override
    public boolean deactivateLong() {
        return candle.getLow() < lastTmpLow
                && (candle.getStart().getMillis() - activeTimeStamp) > DEACTIVATION_CANDLES * candle.getInterval().toDurationMillis();
    }

	 @Override
    public boolean enterLong() {
        return mode == MODE_LONG
					 && candle.getEnd().getMinuteOfDay() > (60 * 8 + 30)
					 && candle.getClosing() > lastTmpHigh
					 && lastTmpLow > previousTmpLow;
    }

	 @Override
    public boolean enterShort() {
        return mode == MODE_SHORT
                && candle.getEnd().getMinuteOfDay() > (60 * 8 + 30)
                //&& prevCandle.getStochasticRSI_SlowK() >= candle.getStochasticRSI_SlowK()
                && candle.getClosing() < lastTmpLow
                && lastTmpHigh < previousTmpHigh;
    }

	 @Override
    public boolean exitShort() {
        return candle.getHigh() > lastTmpHigh && lastTmpLow > previousTmpLow;
    }

	 @Override
    public boolean activateShort() {
        return mode == MODE_NEUTRAL
                && candle.getStochasticRSI_SlowK() >= activeShortTrigger;
    }

	 @Override
    public boolean activateLong() {
        return mode == MODE_NEUTRAL
                && candle.getStochasticRSI_SlowK() <= activeLongTrigger;
    }

	 @Override
    public boolean exitLong() {
        return candle.getLow() < lastTmpLow;
    }

	 @Override
    public boolean deactivateShort() {
        return candle.getHigh() > lastTmpHigh && lastTmpLow > previousTmpLow
                && (candle.getStart().getMillis() - activeTimeStamp) > DEACTIVATION_CANDLES * candle.getInterval().toDurationMillis();
    }

    @Override
    public boolean shouldAdjustStopLossLong() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void adjustStopLossLong() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void adjustStopLossShort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean shouldAdjustStopLossShort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

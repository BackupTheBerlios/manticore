/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.system;
import com.manticore.chart.ChartCanvas;
import com.manticore.position.PositionGrid;
import org.joda.time.DurationFieldType;
import java.util.logging.Level;

public class TradingScript extends TradingSimulation {
    public TradingScript(PositionGrid positionGrid, ChartCanvas chartCanvas) {
        super(chartCanvas);
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


    public void buyShort() {
        Float entry = lastTmpLow;
        Float stopLoss = Math.min(lastTmpHigh, entry + 10);
        Float stopBuy = lastTmpLow;
        Float takeProfit = lastTmpLow - 50;
        buyLimit(stopLoss, entry, stopBuy, takeProfit, getIsinLong());

		  logger.log(Level.INFO, "deactivate short after placing order");
        mode = MODE_NEUTRAL;
    }


    public boolean deactivateLong() {
        return true;
    }


    public boolean enterLong() {
        return false;
    }


    public boolean enterShort() {
        return false;
    }


    public boolean exitShort() {
        return true;
    }


    public boolean activateShort() {
        return false;
    }


    public boolean activateLong() {
        return false;
    }


    public boolean exitLong() {
        return true;
    }


    public boolean deactivateShort() {
        return true;
    }


    public boolean shouldAdjustStopLossLong() {
        return false;
    }


    public void adjustStopLossLong() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public void adjustStopLossShort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public boolean shouldAdjustStopLossShort() {
        return false;
    }
}

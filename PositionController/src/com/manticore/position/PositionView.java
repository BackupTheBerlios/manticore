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

import com.manticore.foundation.Position;
import com.manticore.swingui.FormatedTextField;
import com.manticore.swingui.GridBagPane;
import com.manticore.util.Settings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.text.ParseException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

public class PositionView extends JDialog {
    public final static String takeProfitModeStr[]={"manually", "automatically", "trailing SL", "trailing SL Auto"};
    GridBagPane gridBagPane;
    JTextField underlying;
    JTextField instrument;
    JTextField order;
    JTextField slOrder;

    FormatedTextField underlyingStopLoss;
    FormatedTextField underlyingEntry;
    FormatedTextField underlyingStopEntry;
    FormatedTextField underlyingTarget;
    FormatedTextField availableShares;
    FormatedTextField heldShares;
    FormatedTextField transactionAmount;
    FormatedTextField positionAmount;
    FormatedTextField stop;
    FormatedTextField loss;
    FormatedTextField entry;
    FormatedTextField price;
    FormatedTextField target;
    FormatedTextField profit;
    FormatedTextField ratio;
    FormatedTextField limit;
    FormatedTextField positionProfit;
    FormatedTextField adjustment;

    JToggleButton refreshButton;
    JToggleButton buyButton;
    JToggleButton sellButton;
    JToggleButton stopLossButton;
    JButton orderModeButton;
    JToggleButton cancelButton;
    JToggleButton updateOrderStatus;
    JToggleButton updateSLOrderStatus;
    JToggleButton stopLossEntryButton;
    JButton takeProfitModeButton;
    PositionControler positionControler = null;
    

    public PositionView(PositionControler positionControler) {
        this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        gridBagPane=new GridBagPane();

        this.positionControler = positionControler;

        underlying=new JTextField(positionControler.getUnderlyingDescription());
        underlying.setEditable(false);
        
        instrument = new JTextField();
        instrument.setEditable(false);

        underlyingStopLoss = new FormatedTextField(0, FormatedTextField.DECIMAL_FORMAT, true);
        underlyingStopLoss.setToolTipText("stop loss");

        underlyingEntry = new FormatedTextField(0, FormatedTextField.DECIMAL_FORMAT, true);
        underlyingEntry.setToolTipText("entry");
        
        underlyingStopEntry = new FormatedTextField(0, FormatedTextField.DECIMAL_FORMAT, true);
        underlyingStopEntry.setToolTipText("stop entry");

        underlyingTarget = new FormatedTextField(0, FormatedTextField.DECIMAL_FORMAT, true);
        underlyingTarget.setToolTipText("take profit");
        
        takeProfitModeButton=new JButton();
        takeProfitModeButton.setActionCommand("TAKE_PROFIT_MODE");
        takeProfitModeButton.addActionListener(positionControler);
        takeProfitModeButton.setContentAreaFilled(false);
        takeProfitModeButton.setFocusPainted(false);
        takeProfitModeButton.setMargin(new Insets(0, 0, 0, 0));

        availableShares = new FormatedTextField(new Integer(0), FormatedTextField.INTEGER_FORMAT, false);
        heldShares = new FormatedTextField(new Integer(0), FormatedTextField.INTEGER_FORMAT, false);

        transactionAmount = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);
        positionAmount = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);

        stop = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);
        loss = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);

        entry = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);
        adjustment = new FormatedTextField(new Float(0.001f), FormatedTextField.PERCENT_FORMAT, true);

        price = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, true);
        positionProfit = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);

        target = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);
        profit = new FormatedTextField(new Integer(0), FormatedTextField.DECIMAL_FORMAT, false);

        ratio = new FormatedTextField(new Integer(0), FormatedTextField.PERCENT_FORMAT, false);
        limit = new FormatedTextField(new Integer(0), FormatedTextField.PERCENT_FORMAT, false);

        refreshButton = new JToggleButton("");
        refreshButton.setActionCommand("refresh");
        refreshButton.addActionListener(positionControler);

        buyButton = new JToggleButton("B");
        buyButton.setBackground(Settings.MANTICORE_DARK_BLUE);
        buyButton.setForeground(Settings.MANTICORE_LIGHT_GREY);
        buyButton.setFocusPainted(false);
        buyButton.setMargin(new Insets(0, 0, 0, 0));
        buyButton.setActionCommand("buy");
        buyButton.addActionListener(positionControler);

        sellButton = new JToggleButton("S");
        sellButton.setBackground(Settings.MANTICORE_ORANGE);
        sellButton.setForeground(Settings.MANTICORE_LIGHT_GREY);
        sellButton.setFocusPainted(false);
        sellButton.setMargin(new Insets(0, 0, 0, 0));
        sellButton.setActionCommand("sell");
        sellButton.addActionListener(positionControler);

        stopLossButton = new JToggleButton("s");
        stopLossButton.setActionCommand("stopLoss");
        stopLossButton.setBackground(Settings.MANTICORE_LIGHT_BLUE);
        stopLossButton.setFocusPainted(false);
        stopLossButton.setMargin(new Insets(0, 0, 0, 0));
        stopLossButton.addActionListener(positionControler);

        stopLossEntryButton = new JToggleButton("0");
        stopLossEntryButton.setBackground(Settings.MANTICORE_LIGHT_BLUE);
        stopLossEntryButton.setFocusPainted(false);
        stopLossEntryButton.setMargin(new Insets(0, 0, 0, 0));
        stopLossEntryButton.setActionCommand("stopLossEntry");
        stopLossEntryButton.addActionListener(positionControler);

        orderModeButton=new JButton();
        orderModeButton.setActionCommand("orderMode");
        orderModeButton.setContentAreaFilled(false);
        orderModeButton.addActionListener(positionControler);
        orderModeButton.setFocusPainted(false);
        orderModeButton.setMargin(new Insets(0, 0, 0, 0));

        cancelButton = new JToggleButton("C");
        cancelButton.setBackground(Settings.MANTICORE_DARK_GREY);
        cancelButton.setFocusPainted(false);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.setActionCommand("cancelOrder");
        cancelButton.addActionListener(positionControler);

        updateOrderStatus = new JToggleButton("");
        updateOrderStatus.setActionCommand("updateOrderStatus");
        updateOrderStatus.addActionListener(positionControler);

        updateSLOrderStatus = new JToggleButton("");
        updateSLOrderStatus.setActionCommand("updateSLOrderStatus");
        updateSLOrderStatus.addActionListener(positionControler);

        order=new JTextField();
        order.setEditable(false);
        slOrder=new JTextField();
        slOrder.setEditable(false);

        gridBagPane.add(underlying, "label=underlying:, fill=HORIZONTAL, weightx=1f, gridwidth=2");
        gridBagPane.add(underlyingStopLoss, "nl, label=stop:, fill=HORIZONTAL, weightx=0.5f, gridwidth=1");
        gridBagPane.add(limit, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(underlyingEntry, "nl, label=entry:, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(underlyingStopEntry, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(underlyingTarget, "nl, label=target:, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(takeProfitModeButton, "size=96 18, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(orderModeButton, "size=18 18, weightx=0.0f");

        gridBagPane.add(instrument, "nl, label=warrant:, fill=HORIZONTAL, weightx=1f, gridwidth=2, insets=2 6 2 2");
        gridBagPane.add(refreshButton, "size=18 18, weightx=0.0f, gridwidth=1, tooltip=Calculate warrant");

        gridBagPane.add(availableShares, "nl, label=shares:, fill=HORIZONTAL, weightx=0.5f, insets=2 1 2 2");
        gridBagPane.add(heldShares, "fill=HORIZONTAL, weightx=0.5f");

        gridBagPane.add(transactionAmount, "nl, label=amount:, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(positionAmount, "fill=HORIZONTAL, weightx=0.5f");
        

        gridBagPane.add(stop, "nl, label=stop:, fill=HORIZONTAL, weightx=0.5f, insets=2 6 2 2");
        gridBagPane.add(loss, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(stopLossButton, "size=18 18, weightx=0.0f, tooltip=set Stop Loss on Limit");

        gridBagPane.add(entry, "nl, label=entry:, fill=HORIZONTAL, weightx=0.5f, insets=2 1 2 2");
        gridBagPane.add(adjustment, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(stopLossEntryButton, "size=18 18, weightx=0.0f, tooltip=set SL on Entry");

        gridBagPane.add(price, "nl, label=price:, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(positionProfit, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(buyButton, "size=18 18, weightx=0.0f, tooltip=Buy");

        gridBagPane.add(target, "nl, label=target:, fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(profit, "fill=HORIZONTAL, weightx=0.5f");
        gridBagPane.add(sellButton, "size=18 18, weightx=0.0f, tooltip=Sell");
        

        gridBagPane.add(ratio, "nl, label=ratio:, fill=HORIZONTAL, weightx=0.5f, insets=2 6 2 2");
        
        gridBagPane.add(cancelButton, "dx, size=18 18, weightx=0.0f, tooltip=Cancel Order");

        gridBagPane.add(order, "nl, label=order:, fill=HORIZONTAL, gridwidth=2, insets=2 6 2 2");
        gridBagPane.add(updateOrderStatus, "size=18 18, weightx=0.0f, tooltip=Update Orderstatus");
        gridBagPane.add(slOrder, "nl, label=Stop Loss order:, fill=HORIZONTAL, gridwidth=2, insets=2 1 2 2");
        gridBagPane.add(updateSLOrderStatus, "size=18 18, weightx=0.0f, tooltip=Update Stop Loss Orderstatus");

        //restrict sutton-size
        Dimension maxDimension=new Dimension(18, 18);
        refreshButton.setMaximumSize(maxDimension);
        buyButton.setMaximumSize(maxDimension);
        sellButton.setMaximumSize(maxDimension);
        stopLossButton.setMaximumSize(maxDimension);
        stopLossEntryButton.setMaximumSize(maxDimension);
        updateOrderStatus.setMaximumSize(maxDimension);
        updateSLOrderStatus.setMaximumSize(maxDimension);

        setPreferredSize(new Dimension(320, 380));
        getContentPane().add(gridBagPane);

        setTitle(positionControler.getUnderlyingDescription());
        setAlwaysOnTop(true);
        Toolkit tk = Toolkit.getDefaultToolkit();
        setLocation((tk.getScreenSize().width - getWidth()) / 2, (tk.getScreenSize().height - getHeight()) / 2);
        pack();
        setVisible(true);
    }

    public Position getPosition(Position position) throws ParseException {
        position.setUnderlyingStopLoss(underlyingStopLoss.getFloatValue());
        position.setUnderlyingEntry(underlyingEntry.getFloatValue());
        position.setUnderlyingStopEntry(underlyingStopEntry.getFloatValue());
        position.setUnderlyingTarget(underlyingTarget.getFloatValue());
        position.setAdjustment(adjustment.getFloatValue());
        position.derivePositionType();
        return position;
    }

    public void setPosition(Position position) {
        instrument.setText(position.getDescription());

        //underlyingStopEntry.setValue(position.getOptimalEntry());

        underlyingStopLoss.setValue(position.underlyingStopLoss);
        underlyingEntry.setValue(position.underlyingEntry);
        underlyingTarget.setValue(position.underlyingTarget);

        if (position.getWaveXXL()!=null) {
            stop.setValue(position.getStopLoss());
            loss.setValue(position.getLoss());


            entry.setValue(position.getEntry());
            adjustment.setValue(position.getAdjustment());
            price.setValue(position.getMarketPrice());
            positionProfit.setValue(position.getPositionProfit());

            target.setValue(position.getTarget());
            profit.setValue(position.getProfit());

            ratio.setValue(position.getRatio());
            limit.setValue(position.getLimit());

            availableShares.setValue(position.getAvailableShares());
            heldShares.setValue(position.getQuantity());
            transactionAmount.setValue(position.getTransactionAmount());
            positionAmount.setValue(position.getAmount());
            positionProfit.setValue(position.getPositionProfit());
        }
        refreshButton.setSelected(false);
    }

    public void setOrderMode(int orderMode) {
        if (orderMode==PositionControler.ORDER_MODE_DIRECT) {
            orderModeButton.setText("D");
            orderModeButton.setToolTipText("direct order");
        }
        else if (orderMode==PositionControler.ORDER_MODE_LIMIT) {
            orderModeButton.setText("L");
            orderModeButton.setToolTipText("limit order");
        }
        else if (orderMode==PositionControler.ORDER_MODE_LIMIT_STOP) {
            orderModeButton.setText("S");
            orderModeButton.setToolTipText("stop limit order");
        } else if (orderMode==PositionControler.ORDER_MODE_NOLIMIT) {
            orderModeButton.setText("U");
            orderModeButton.setToolTipText("unlimited order");
        }

        if (orderMode==PositionControler.ORDER_MODE_LIMIT_STOP) {
            underlyingStopEntry.setVisible(true);
        } else {
            underlyingStopEntry.setVisible(false);
        }
    }

    public void setTakeProfitMode(int mode) {
        if (mode==PositionControler.TAKE_PROFIT_MANUAL) {
            takeProfitModeButton.setText("manually");
            takeProfitModeButton.setToolTipText("Will not do anything if target is reached.");
        }        else if (mode==PositionControler.TAKE_PROFIT_TRAILING) {
            takeProfitModeButton.setText("trailing SL");
            takeProfitModeButton.setToolTipText("Adjust trailing stop loss automatically.");
        }        else if (mode==PositionControler.TAKE_PROFIT_AUTOMATIC) {
            takeProfitModeButton.setText("automatic TP");
            takeProfitModeButton.setToolTipText("Take profit automatically when target is reached.");
        } else if (mode==PositionControler.TAKE_PROFIT_TRAILING_AUTOMATIC) {
            takeProfitModeButton.setText("TSL + automatic TP");
            takeProfitModeButton.setToolTipText("Adjust trailing stop loss and take profit automatically when target is reached.");
        }
    }

    public void setOrder(String description) {
        order.setText(description);
        order.setToolTipText(description);
    }

    public void setSLOrder(String description) {
        slOrder.setText(description);
        slOrder.setToolTipText(description);
    }

    public void resetSellButton() {
        sellButton.setEnabled(false);
        sellButton.setSelected(false);
        sellButton.setEnabled(true);
    }
    
    public void resetBuyButton() {
        buyButton.setEnabled(false);
        buyButton.setSelected(false);
        buyButton.setEnabled(true);
    }

    public void resetRefreshButton() {
        refreshButton.setEnabled(false);
        refreshButton.setSelected(false);
        refreshButton.setEnabled(true);
    }

    public void resetCancelButton() {
        cancelButton.setEnabled(false);
        cancelButton.setSelected(false);
        cancelButton.setEnabled(true);
    }

    public void resetUpdateOrderButton() {
        updateOrderStatus.setEnabled(false);
        updateOrderStatus.setSelected(false);
        updateOrderStatus.setEnabled(true);
    }

    public void resetUpdateSLOrderButton() {
        updateSLOrderStatus.setEnabled(false);
        updateSLOrderStatus.setSelected(false);
        updateSLOrderStatus.setEnabled(true);
    }

    public void resetStopLossButton() {
        stopLossButton.setEnabled(false);
        stopLossButton.setSelected(false);
        stopLossButton.setEnabled(true);
    }

    public void resetStopLossEntryButton() {
        stopLossEntryButton.setEnabled(false);
        stopLossEntryButton.setSelected(false);
        stopLossEntryButton.setEnabled(true);
    }
}

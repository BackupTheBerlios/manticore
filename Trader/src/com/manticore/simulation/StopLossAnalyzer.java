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
package com.manticore.simulation;

import com.manticore.foundation.Tick;
import com.manticore.database.Quotes;
import com.manticore.foundation.Transaction;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Formatter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class StopLossAnalyzer extends Thread {

    String fileName;
    private ArrayList<Tick> tickArrayList;
    private ArrayList<Transaction> transactionArrayList;
    static final Pattern ORDER_ID = Pattern.compile("\\s([\\d]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern ORDER_DATE = Pattern.compile("([\\d\\.]+)\\s", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern ORDER_TIME = Pattern.compile("([\\d\\:]+)\\s", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern ORDER_TYPE = Pattern.compile("\\s([A-Za-z]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern ORDER_STATE = Pattern.compile("([A-Za-zü]+)[\\s]+DE", Pattern.LITERAL);
    static final Pattern ASSET_TYPE = Pattern.compile("DEUT\\.BANK WXXL([CP]) DAX", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    static final Pattern ASSET_PRICE = Pattern.compile("\\s([\\d\\.,]+) EUR\\s", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);

    public StopLossAnalyzer() {
            DateTime dateTimeFrom=Quotes.SQL_DATE_TIME_FORMATTER.parseDateTime("2009-01-01 00:00:00");
            DateTime dateTimeTo=Quotes.SQL_DATE_TIME_FORMATTER.parseDateTime("2009-03-22 22:00:00");
            
            transactionArrayList=Quotes.getInstance().getTransactionArrayList(133962, dateTimeFrom, dateTimeTo);
            tickArrayList = Quotes.getInstance().getQuotesTickArrayList(133962, 57, dateTimeFrom);
            run();
    }

    public StopLossAnalyzer(ArrayList<Transaction> transactionArrayList, long id_instrument, long id_stock_exchange, DateTime dateTimeFrom) {
        this.transactionArrayList=transactionArrayList;
        this.tickArrayList = Quotes.getInstance().getQuotesTickArrayList(id_instrument, id_stock_exchange, dateTimeFrom);
    }

    public void printTransactionArrayList() {
        Formatter formatter = new Formatter((OutputStream) System.out);
        formatter.format("| timestamp     | C/P    | price   | quantity   | %n");
        formatter.format("|---------------+--------+---------+------------| %n");

        for (int i=0; i<transactionArrayList.size(); i++) {
             formatter.format(" %1$tF %1$tT |", transactionArrayList.get(i).timestamp );
             formatter.format(" %s |", transactionArrayList.get(i).id_transaction_type );
             formatter.format(" %6.4f |", transactionArrayList.get(i).price );
             formatter.format(" %6d | %n", transactionArrayList.get(i).quantity );
        }
        formatter.flush();
        formatter.close();
    }

    public void printTickArrayList() {
        Formatter formatter = new Formatter((OutputStream) System.out);
        formatter.format("| timestamp     | price   | %n");
        formatter.format("|---------------+---------| %n");

        for (int i=0; i<tickArrayList .size(); i++) {
             formatter.format(" %1$tF %1$tT |", tickArrayList.get(i).getDateTime().toDate() );
             formatter.format(" %6.2f %n", tickArrayList.get(i).getPrice() );
        }
        formatter.flush();
        formatter.close();
    }


    public void run()  {
        Float initialStopLoss = 0.003f;
        Float trailingStopLoss = 0.03f;
        Float maximumDrawDown = 100f;
        Formatter formatter = new Formatter((OutputStream) System.out);
        formatter.format("ISL     | TSL    | Result  | Quote   | max PL  | min PL  | max Duration | min Duration%n");
        formatter.format("--------+--------+---------+---------+---------+---------+--------------+-------------%n");

        PeriodFormatter periodFormatter = new PeriodFormatterBuilder().printZeroNever().appendDays().appendSuffix("d").printZeroAlways().appendHours().appendSeparator(":").minimumPrintedDigits(2).appendMinutes().appendSeparator(":").appendSeconds().toFormatter();

        for (int  k =  0; k< 20;k++) {
            trailingStopLoss = 0.00f;
            initialStopLoss += 0.0005f;

            for (int i = 0; i < 20; i++) {
                Float result = 0.0f;
                Integer winTrades = 0;
                Integer lossTrades = 0;
                Float maxWin = null;
                Float maxLoss = null;
                Duration maxDuration = null;
                Duration minDuration = null;

                trailingStopLoss += 0.0005f;

                ArrayList<Trade> tradeArrayList = initiateTradeArrayList(initialStopLoss, trailingStopLoss, maximumDrawDown);

                Iterator<Trade> tradeIterator = tradeArrayList.iterator();

                while (tradeIterator.hasNext()) {
                    tradeIterator.next().start();
                }

                tradeIterator = tradeArrayList.iterator();
                while (tradeIterator.hasNext()) {
                    try {
                        Trade trade = tradeIterator.next();
                        trade.join();
                        Float tradeResult = trade.getResult();
                        Duration tradeDuration = trade.getDuration();
                        if (tradeResult >= 0) {
                            winTrades++;
                            if (maxWin == null || maxWin < tradeResult) {
                                maxWin = tradeResult;
                            }
                        } else {
                            lossTrades++;
                            if (maxLoss == null || maxLoss > tradeResult) {
                                maxLoss = tradeResult;
                            }
                        }
                        if (minDuration == null || tradeDuration.isShorterThan(minDuration)) {
                            minDuration = tradeDuration;
                        }
                        if (maxDuration == null || tradeDuration.isLongerThan(maxDuration)) {
                            maxDuration = tradeDuration;
                        }
                        result += trade.getResult();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(StopLossAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }


                formatter.format(" %6.4f |", initialStopLoss);
                formatter.format(" %6.4f |", trailingStopLoss);
                formatter.format("%8.2f |", result);
                formatter.format("%8.2f |", (float) winTrades / (float) lossTrades);
                formatter.format("%8.2f |", maxWin);
                formatter.format("%8.2f |", maxLoss);
                formatter.format("%13s |", periodFormatter.print(maxDuration.toPeriod()));
                formatter.format("%13s %n", periodFormatter.print(minDuration.toPeriod()));
                formatter.flush();

            }
            formatter.format("--------+--------+---------+---------+---------+---------+--------------+-------------%n");
        }
        formatter.close();


        System.exit(0);
    }

    public void simulate2() throws InterruptedException {
        Float initialStopLoss = 0.0035f;
        Float trailingStopLoss = 0.0075f;
        Float maximumDrawDown = 100f;

        Formatter formatter = new Formatter((OutputStream) System.out);
        formatter.format("Type    | Buy Date           | Buy Price | Sell Date          | Sell Price | Result   %n");
        formatter.format("--------+--------------------+-----------+--------------------+------------+----------%n");

        ArrayList<Trade> tradeArrayList = initiateTradeArrayList(initialStopLoss, trailingStopLoss, maximumDrawDown);
        Iterator<Trade> tradeIterator = tradeArrayList.iterator();

        while (tradeIterator.hasNext()) {
            tradeIterator.next().start();
        }

        tradeIterator = tradeArrayList.iterator();
        while (tradeIterator.hasNext()) {
            Trade trade = tradeIterator.next();
            trade.join();

            formatter.format("%7s |", trade.getType());
            formatter.format("%16s |", DateTimeFormat.mediumDateTime().print(trade.getBuyDateTime()));
            formatter.format("%10.2f |", trade.getBuyPrice());
            formatter.format("%16s |", DateTimeFormat.mediumDateTime().print(trade.getSellDateTime()));
            formatter.format("%11.2f |", trade.getSellPrice());
            formatter.format("%10.2f |", trade.getResult());
            formatter.format("%n");
            formatter.flush();

        }
        formatter.format("%n");
        formatter.close();
        System.exit(0);
    }

    public void simulate1() throws  InterruptedException {
        Float initialStopLoss = 0.06f;
        Float trailingStopLoss = 0.005f;
        Float maximumDrawDown = 100f;

        Formatter formatter = new Formatter((OutputStream) System.out);

        formatter.format("Type    | Buy Date           | Buy Price | Sell Date          | Sell Price | Result   %n");
        formatter.format("--------+--------------------+-----------+--------------------+------------+----------%n");

        Trade trade = new Trade(tickArrayList, "C", "03.02.2009 09:38:35", initialStopLoss, trailingStopLoss, maximumDrawDown);
        trade.start();
        trade.join();

        formatter.format("%7s |", trade.getType());
        formatter.format("%16s |", DateTimeFormat.mediumDateTime().print(trade.getBuyDateTime()));
        formatter.format("%10.2f |", trade.getBuyPrice());
        formatter.format("%16s |", DateTimeFormat.mediumDateTime().print(trade.getSellDateTime()));
        formatter.format("%11.2f |", trade.getSellPrice());
        formatter.format("%10.2f |", trade.getResult());
        formatter.format("%n%n");
        formatter.flush();

        System.exit(0);
    }

    private static final String extractString(String s, Pattern p) {
        if (s.length() > 0) {
            Matcher m = p.matcher(s);
            if (m.find()) {
                s = m.group(1);
            }
        }
        return s;
    }

    private ArrayList<Trade> readTransactions(Float initialStoppLoss, Float trailingStoppLoss, Float maximumDrawDown) throws FileNotFoundException, IOException {
        ArrayList<Trade> tradeArrayList = new ArrayList();

        FileInputStream fstream = new FileInputStream(fileName);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String strLine;
        int i = 0;
        String orderIDStr = "";
        String orderDateTimeStr = "";
        String orderType = "";
        String orderState = "";
        String assetType = "";
        String assetPriceStr = "";

        while ((strLine = br.readLine()) != null) {
            if (i == 0) {
                orderIDStr = extractString(strLine, ORDER_ID);

            } else if (i == 1) {
                orderDateTimeStr = extractString(strLine, ORDER_DATE);
            } else if (i == 2) {
                orderDateTimeStr += " " + extractString(strLine, ORDER_TIME);
                orderType = extractString(strLine, ORDER_TYPE);
            } else if (i == 3) {
                orderState = extractString(strLine, ORDER_STATE);
            } else if (i == 4) {
                assetType = extractString(strLine, ASSET_TYPE);
            } else if (i == 6) {
                assetPriceStr = extractString(strLine, ASSET_PRICE);
            } else if (i == 8 && orderState.contains("ausgeführt") && orderType.equals("Kauf")) {
                Trade trade = new Trade(tickArrayList, assetType, orderDateTimeStr, initialStoppLoss, trailingStoppLoss, maximumDrawDown);
                tradeArrayList.add(trade);
            }

            i++;

            if (i == 9) {
                i = 0;
            }
        }
        in.close();

        return tradeArrayList;
    }

    private ArrayList<Trade> initiateTradeArrayList(Float initialStoppLoss, Float trailingStoppLoss, Float maximumDrawDown) {
        ArrayList<Trade> tradeArrayList = new ArrayList();

        Iterator<Transaction> transactionIterator=transactionArrayList.iterator();
        while (transactionIterator.hasNext()) {
            Transaction transaction=transactionIterator.next();

            //just add the buys
            if (transaction.quantity>0) {
                Trade trade = new Trade(tickArrayList, transaction, initialStoppLoss, trailingStoppLoss, maximumDrawDown);
                tradeArrayList.add(trade);
            }
        }
        return tradeArrayList;
    }
}

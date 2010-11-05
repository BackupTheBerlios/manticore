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
import com.manticore.chart.ChartParameters;
import com.manticore.chart.PeriodSettings;
import com.manticore.database.Quotes;
import com.manticore.foundation.Candle;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;
import com.manticore.foundation.StockExchange;
import com.manticore.foundation.Transaction;
import com.manticore.report.PerformanceReport;
import com.manticore.util.ThreadArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;

public class TradingSimulation {
    public final static Logger logger=Logger.getLogger(TradingSimulation.class.getName());
    public final static long id_account=2;
    public long id_transaction=0;

    Instrument instrument;
    StockExchange stockExchange;
    PeriodSettings periodSettings;
    ChartParameters chartParameters;
    CandleArrayList candleArrayList;
    HashMap<Long, PositionA> positionHashMap;



    public static void main(String[] args) {
        TradingSimulation system=new TradingSimulation();
        system.run();

        PerformanceReport performanceReport= new PerformanceReport("simulation");
        //performanceReport.set
    }

    public TradingSimulation() {
        positionHashMap=new HashMap<Long, PositionA>();

        instrument=Quotes.getInstance().getInstrumentFromID(1);
        stockExchange=Quotes.getInstance().getStockExchange(22);
        DateTime dateTimeTo=DateTimeFormat.mediumDateTime().parseDateTime("15.10.2010 22:00:00");
        periodSettings=new PeriodSettings(DurationFieldType.days(),180, DurationFieldType.days(), 1, DurationFieldType.hours(), 2, DurationFieldType.minutes(), 15);

        chartParameters=new ChartParameters(0, instrument, stockExchange, dateTimeTo, periodSettings);
        candleArrayList=new CandleArrayList(chartParameters);
    }

    private void cleanUp() {
        String sqlStr=new StringBuffer()
                .append("delete ")
                .append("from simulation.transaction ")
                .append("where id_position in ")
                .append("( ")
                .append("   select ")
                .append("   id_position ")
                .append("   from simulation.position ")
                .append("   where id_account=").append(id_account)
                .append(") ")
                .append("; ")
                .append("delete ")
                .append("from simulation.position ")
                .append("where id_account=").append(id_account)
                .append("; ")
                .toString();
       logger.info( Quotes.executeUpdate(sqlStr) + " records deleted.");
    }
    
    public void run() {
        cleanUp();

        Float lastLow=null;
        Float previousLow=null;
        Float lastHigh=null;
        Float previousHigh=null;
        int mode=0; // 1 - long, 0- neutral, -1 short
        float activeLongTrigger=3f;
        float activeShortTrigger=97f;

        float lowerTrigger=30f;
        float upperTrigger=75f;
        float sl=0.0045f;

        long activeTimeStamp=0;

        for (int i=1; i<candleArrayList.size(); i++) {
            Candle candle=candleArrayList.get(i);
            processTransactions(candle.getStart().toDate(), candle.getEnd().toDate());

            if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_HIGH) {
                previousHigh=lastHigh;
                lastHigh=candle.getHigh();
            } else if (candle.getLocalExtremum()==Candle.LOCAL_EXTREMUM_LOW) {
                previousLow=lastLow;
                lastLow=candle.getLow();
            }

            if (candleArrayList.get(i).getStochasticRSI_SlowK()!=null && candleArrayList.get(i-1).getStochasticRSI_SlowK()!=null) {
//
//                if (mode==0 && candle.getStochasticRSI_SlowK()<=activeLongTrigger) {
//                    mode=1;
//                    activeTimeStamp=candle.getStart().getMillis();
//                }

                if(mode == 0 && candle.getStochasticRSI_SlowK() >= activeShortTrigger) {
                    mode=-1;
                    activeTimeStamp=candle.getStart().getMillis();
                }
                
                if (mode==1
                        //&& candle.getStochasticRSI_SlowK()>=upperTrigger
                        && candle.getEnd().getMinuteOfDay()>(60*8+30)
                        && candleArrayList.get(i-1).getStochasticRSI_SlowK()<=candleArrayList.get(i-1).getStochasticRSI_SlowK()
                        && candle.getClosing()>lastHigh
                        && lastLow>previousLow
                        ) {
                    logger.info("open new position at " + candle.getEnd().toDate() + " " + candle.getStochasticRSI_SlowK());
                    //Float entry=candle.getHigh();
                    Float entry=lastHigh;
                    
                    //Float stopLoss=Math.max(candleArrayList.get(i).getLow(), entry*0.995f);
                    Float stopLoss=Math.max(lastLow, entry*(1-sl));

                    openNewPosition(candle.getEnd().toDate(), Position.POSITION_TYPE_CALL, entry, stopLoss );
                    mode=0;
                }

                if (mode==-1
                        //&& candle.getStochasticRSI_SlowK()>=upperTrigger
                        && candle.getEnd().getMinuteOfDay()>(60*8+30)
                        && candleArrayList.get(i-1).getStochasticRSI_SlowK()<=candleArrayList.get(i-1).getStochasticRSI_SlowK()
                        && candle.getClosing()<lastLow
                        && lastHigh<previousHigh
                        ) {
                    logger.info("open new position at " + candle.getEnd().toDate() + " " + candle.getStochasticRSI_SlowK());
                    //Float entry=candle.getHigh();
                    Float entry=lastLow;

                    //Float stopLoss=Math.max(candleArrayList.get(i).getLow(), entry*0.995f);
                    Float stopLoss=Math.min(lastHigh, entry*(1+sl));

                    openNewPosition(candle.getEnd().toDate(), Position.POSITION_TYPE_PUT, entry, stopLoss );
                    mode=0;
                }

//                if (
//                        (mode==1 && candle.getLow() < lastLow)
//                        || (mode==-1 && candle.getHigh() > lastHigh)
//                        ) {
//                    closePositions(candle.getEnd().toDate(), candle.getClosing());
//                    mode=0;
//                }

                if (candle.getLow() < lastLow) {
                    closeLongPositions(candle.getEnd().toDate(), candle.getClosing());
                     if (mode==1) mode=0;
                }

                if (candle.getHigh() > lastHigh && lastLow>previousLow) {
                    closeShortPositions(candle.getEnd().toDate(), candle.getClosing());

                    if (mode==-1) mode=0;
                }


                if  ( mode!=0 &&
                        (candle.getStart().getMillis()-activeTimeStamp)>3*15*60*1000
                        ) {
                    mode=0;
                }
            }

            
            if (candle.getEnd().getMinuteOfDay()>1300) {
                mode=0;
                closeAll(candle.getEnd().toDate(), candle.getClosing());
            }
        }

        closePositions(candleArrayList.getLastValidCandle().getEnd().toDate(), candleArrayList.getLastValidCandle().getClosing());

        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        while (positionIterator.hasNext()) {
            Position position=positionIterator.next();
            Quotes.getInstance().writePosition("simulation", position);

            Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
            while (transactionIterator.hasNext()) Quotes.getInstance().writeTransaction("simulation", transactionIterator.next());
        }
    }

    private void processTransactions(Date dateFrom, Date dateTo) {
        try {
            final ResultSet rs = Quotes.getInstance().getTickdataResultSet(instrument.id_instrument, stockExchange.getId(), dateFrom, dateTo);
            while (rs.next()) {
                updateTransactions(rs.getTimestamp("timestamp"), rs.getFloat("price"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(TradingSimulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class TransactionUpdateThread extends Thread {
        private Position position;
        private Date timestamp;
        private Float price;

        TransactionUpdateThread(Position position, Date timestamp, Float price) {
            this.position=position;
            this.timestamp=timestamp;
            this.price=price;
        }

        @Override
        public void run() {
            if (position.isOpen()) {
                Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
                while (transactionIterator.hasNext()) {
                    Transaction transaction=transactionIterator.next();
                    if (transaction.isOpen() && transaction.quantity>0 && ((position.isLong() && transaction.price<=price) || (position.isShort() && transaction.price>=price)) ) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_EXECUTED;
                        transaction.price=price;
                        transaction.timestamp=timestamp;

                        position.buy(transaction.price, transaction.quantity);

                        //@todo: adjust SL
                        id_transaction++;
                        Transaction transactionSL=new Transaction(position.id_position, String.valueOf(id_transaction), Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, timestamp, position.underlyingStopLoss, 0f, -position.quantity);
                        position.getTransactionHashMap().put(transactionSL.id_transaction, transactionSL);

                    } else if (transaction.isOpen() && transaction.quantity<0 && ((position.isLong() && transaction.price>=price) || (position.isShort() && transaction.price<=price))) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_EXECUTED;
                        transaction.price=price;
                        transaction.timestamp=timestamp;

                        position.sell(transaction.price, transaction.quantity);
                    }
                }
            }
        }
    }

    private void updateTransactions(Date timestamp, Float price) {
        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        ThreadArrayList threadArrayList=new ThreadArrayList(positionHashMap.values().size());

        while (positionIterator.hasNext()) {
            threadArrayList.addThread(new TransactionUpdateThread(positionIterator.next(), timestamp, price));
        }
        threadArrayList.join();
    }

    private void closeAll(Date timestamp, Float price) {
        if (price!=null) {

        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        while (positionIterator.hasNext()) {
            PositionA position=positionIterator.next();
            if (position.isOpen()) {
                Logger.getAnonymousLogger().info("profit: " + position.getPositionProfit(price));
                Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
                while (transactionIterator.hasNext()) {
                    Transaction transaction=transactionIterator.next();
                    if (transaction.isOpen() && transaction.quantity>0) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_CANCELED;
                        transaction.timestamp=timestamp;
                    } else if (transaction.isOpen() && transaction.quantity<0) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_EXECUTED;
                        transaction.price=price;
                        transaction.timestamp=timestamp;

                        position.sell(transaction.price, transaction.quantity);
                    }
                }
            }
        }
        }
    }

    private void openNewPosition(Date timestamp, String id_position_type, Float underlyingEntry, Float underlyingStop) {
        long id_position=Quotes.getInstance().getNextPositionID("simulation");

        logger.info("open new position at " + timestamp + " " + underlyingEntry);

        PositionA position=new PositionA(id_account, id_position, instrument, underlyingStop, underlyingEntry, 0f);
        position.isin="FFFFFF";
        position.id_position_type=id_position_type;
        
        id_transaction++;
        Transaction transaction=new Transaction(id_position, String.valueOf(id_transaction), Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, timestamp, underlyingEntry, 0f, 1L);
        position.getTransactionHashMap().put(transaction.id_transaction, transaction);
        positionHashMap.put(id_position, position);
    }

    private void closePositions(Date timestamp, Float price) {
        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        while (positionIterator.hasNext()) {
            Position position=positionIterator.next();
            if (position.isOpen()) {

                Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
                while (transactionIterator.hasNext()) {
                    Transaction transaction=transactionIterator.next();
                    if (transaction.isOpen() && transaction.quantity>0) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_CANCELED;
                        transaction.timestamp=timestamp;
                    }
                    if (transaction.isOpen() && transaction.quantity<0) {
                        transaction.price=price;
                        transaction.timestamp=timestamp;
                    }
                }

                logger.info("close position at " + timestamp + " " + price);
            }
        }
    }

    private void closeLongPositions(Date timestamp, Float price) {
        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        while (positionIterator.hasNext()) {
            Position position=positionIterator.next();
            if (position.isOpen() && position.isLong()) {

                Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
                while (transactionIterator.hasNext()) {
                    Transaction transaction=transactionIterator.next();
                    if (transaction.isOpen() && transaction.quantity>0) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_CANCELED;
                        transaction.timestamp=timestamp;
                    }
                    if (transaction.isOpen() && transaction.quantity<0) {
                        transaction.price=price;
                        transaction.timestamp=timestamp;
                    }
                }

                logger.info("close position at " + timestamp + " " + price);
            }
        }
    }

    private void closeShortPositions(Date timestamp, Float price) {
        Iterator<PositionA> positionIterator=positionHashMap.values().iterator();
        while (positionIterator.hasNext()) {
            Position position=positionIterator.next();
            if (position.isOpen() && position.isShort()) {

                Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
                while (transactionIterator.hasNext()) {
                    Transaction transaction=transactionIterator.next();
                    if (transaction.isOpen() && transaction.quantity>0) {
                        transaction.id_status=Transaction.TRANSACTION_STATUS_CANCELED;
                        transaction.timestamp=timestamp;
                    }
                    if (transaction.isOpen() && transaction.quantity<0) {
                        transaction.price=price;
                        transaction.timestamp=timestamp;
                    }
                }

                logger.info("close position at " + timestamp + " " + price);
            }
        }
    }
}

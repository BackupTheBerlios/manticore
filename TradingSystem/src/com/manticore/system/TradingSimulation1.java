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
import com.manticore.chart.PeriodSettings;
import com.manticore.database.FakeTickDataTimerTask;
import com.manticore.database.Quotes;
import com.manticore.foundation.Extremum;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;
import com.manticore.foundation.StockExchange;
import com.manticore.foundation.Tick;
import com.manticore.foundation.Transaction;
import com.manticore.position.PositionGrid;
import com.manticore.report.PerformanceReport;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;

public class TradingSimulation1 extends TradingSystem {

	 public final static long id_account = 2;
	 public long id_transaction = 0;
	 public long id_stock_exchange;
	 public long id_instrument;
	 private HashMap<Long, PositionA> positionHashMap;
	 private FakeTickDataTimerTask fakeTickDataTimerTask;


	 public static void main(String[] args) {
		  PositionGrid positionGrid = null;

		  //TradingSimulation1 system = new TradingSimulation1();

		  PerformanceReport performanceReport = new PerformanceReport("simulation");
		  performanceReport.setVisible(true);
	 }

	 public TradingSimulation1(ChartCanvas chartCanvas) {
		  Instrument instrument = Quotes.getInstance().getInstrumentFromID(1);
		  StockExchange stockExchange = Quotes.getInstance().getStockExchange(22);

		  chartParameters = new ChartParameters(0, instrument, stockExchange, getDateTimeTo(stockExchange), getPeriodSettings());
		  candleArrayList = new CandleArrayList(chartParameters);

		  this.chartCanvas=chartCanvas;
		  if (this.chartCanvas!=null) {
				chartCanvas.setChartParameters(chartParameters);
				chartCanvas.updateChartSettings();
				chartCanvas.drawChart();
		  }

		  extremumArrayList = candleArrayList.getExtremumArrayList();

		  positionHashMap = new HashMap<Long, PositionA>();

		  id_stock_exchange = chartParameters.getStockExchange().getId();
		  id_instrument = chartParameters.getInstrument().getId();

		  cleanUp();

		  lastUpdate = candleArrayList.getDateTimeFrom().toDateTime();
		  lastCandleEnd=lastUpdate;

		  logger.info("start on:" + lastCandleEnd);

		  fakeTickDataTimerTask = new FakeTickDataTimerTask(id_instrument, id_stock_exchange, candleArrayList.getDateTimeFrom().toDate(), candleArrayList.getDateTimeTo().toDate());
		  fakeTickDataTimerTask.addChangeListener(this);
		  fakeTickDataTimerTask.start();
	 }

	 public static PeriodSettings getPeriodSettings() {
		  return new PeriodSettings(DurationFieldType.days(), 10, DurationFieldType.days(), 1, DurationFieldType.hours(), 2, DurationFieldType.minutes(), 15);
	 }

	 public static DateTime getDateTimeTo(StockExchange stockExchange) {
		  MutableDateTime dateTimeTo = new MutableDateTime();
		  dateTimeTo.setMinuteOfDay(stockExchange.getClosingMinute());
		  dateTimeTo.setSecondOfMinute(0);
		  dateTimeTo.setMillisOfSecond(0);

		  return dateTimeTo.toDateTime();
	 }

	 public static void cleanUp() {
		  String sqlStr = new StringBuffer().append("delete ").append("from simulation.transaction ").append("where id_position in ").append("( ").append("   select ").append("   id_position ").append("   from simulation.position ").append("   where id_account=").append(id_account).append(") ").append("; ").append("delete ").append("from simulation.position ").append("where id_account=").append(id_account).append("; ").toString();
		  logger.info(Quotes.executeUpdate(sqlStr) + " records deleted.");
	 }

	 @Override
	 public void stateChanged(ChangeEvent e) {
		  if (e.getSource().equals(fakeTickDataTimerTask)) {
				lastTick = fakeTickDataTimerTask.getTick();

				if (lastTick.getDateTime().isAfter(lastCandleEnd)) {
					 candle = candleArrayList.getCandleFromTick(lastTick);

					 if (candle != null) {
						  lastCandleEnd = candle.getEnd();

						  Extremum extremum = extremumArrayList.getLastTmpHigh(lastTick.getDateTime());
						  if (extremum != null) {
								lastTmpHigh = extremum.getPrice();
						  }

						  extremum = extremumArrayList.getPreviousTmpHigh(lastTick.getDateTime());
						  if (extremum != null) {
								previousTmpHigh = extremum.getPrice();
						  }

						  extremum = extremumArrayList.getLastTmpLow(lastTick.getDateTime());
						  if (extremum != null) {
								lastTmpLow = extremum.getPrice();
						  }

						  extremum = extremumArrayList.getPreviousTmpLow(lastTick.getDateTime());
						  if (extremum != null) {
								previousTmpLow = extremum.getPrice();
						  }
					 }
				}

				if (candle != null) {
					 processPositions(lastTick);
					 findSignals();
				}
		  }
	 }

	 public void buyLimit(Float stopLoss, Float entry, Float stopBuy, Float takeProfit, String isin) {
		  if (isin.length() > 0 && lastUpdate.isBefore(candle.getEnd())) {

				String id_position_type = (isin.equals(ISIN_LONG)) ? PositionA.POSITION_TYPE_CALL : PositionA.POSITION_TYPE_PUT;

				openNewPosition(lastTick.getDate(), id_position_type, entry, stopLoss);
				lastUpdate = candle.getEnd();
		  }
	 }

	 public void sellDirect(int mode, Float stopLoss, Float takeProfit) {
		  Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
		  while (positionIterator.hasNext()) {
				PositionA position = positionIterator.next();
				if (position.isOpen() && (position.isLong() && mode == MODE_LONG || position.isShort() && mode == MODE_SHORT)) {

					 Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
					 while (transactionIterator.hasNext()) {
						  Transaction transaction = transactionIterator.next();
						  if (transaction.isOpen() && transaction.quantity > 0) {
								transaction.id_status = Transaction.TRANSACTION_STATUS_CANCELED;
								transaction.timestamp = lastTick.getDate();
						  }
						  if (transaction.isOpen() && transaction.quantity < 0) {
								transaction.price = lastTick.getPrice();
								transaction.timestamp = lastTick.getDate();
						  }
					 }

					 logger.info("close position at " + lastTick.getDate() + " " + lastTick.getPrice());
				}
		  }
		  lastUpdate = candle.getEnd();
	 }

	 public void cancel(int mode) {
		  if (lastUpdate.isBefore(candle.getEnd())) {
				Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
				while (positionIterator.hasNext()) {
					 PositionA position = positionIterator.next();
					 if ((position.isLong() && mode == MODE_LONG) || (position.isShort() && mode == MODE_SHORT)) {

						  Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
						  while (transactionIterator.hasNext()) {
								Transaction transaction = transactionIterator.next();
								if (transaction.isOpen() && transaction.isPurchase()) {
									 transaction.id_status = Transaction.TRANSACTION_STATUS_CANCELED;
									 transaction.timestamp = lastTick.getDateTime().toDate();
								}
						  }
					 }
				}
				lastUpdate = candle.getEnd();
		  }
	 }

	 public void adjustStopLoss(int mode, Float stopLoss) {
		  if (lastUpdate.isBefore(candle.getEnd())) {
				Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
				while (positionIterator.hasNext()) {
					 PositionA position = positionIterator.next();
					 if ((position.isLong() && mode == MODE_LONG) || (position.isShort() && mode == MODE_SHORT)) {

						  Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
						  while (transactionIterator.hasNext()) {
								Transaction transaction = transactionIterator.next();
								if (transaction.isOpen() && transaction.isSale() && transaction.isStop()) {
									 transaction.price = stopLoss;
									 transaction.timestamp = lastTick.getDateTime().toDate();
								}
						  }
					 }
				}
				lastUpdate = candle.getEnd();
		  }
	 }

	 public void sellEOD() {
		  Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
		  while (positionIterator.hasNext()) {
				PositionA position = positionIterator.next();
				if (position.isOpen()) {
					 Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
					 while (transactionIterator.hasNext()) {
						  Transaction transaction = transactionIterator.next();
						  if (transaction.isOpen() && transaction.quantity > 0) {
								transaction.id_status = Transaction.TRANSACTION_STATUS_CANCELED;
								transaction.timestamp = lastTick.getDate();
						  }
						  if (transaction.isOpen() && transaction.quantity < 0) {
								transaction.price = lastTick.getPrice();
								transaction.timestamp = lastTick.getDate();
						  }
					 }

					 logger.info("close position at " + lastTick.getDate() + " " + lastTick.getPrice());
				}
		  }
	 }

	 public void stopSystem() {
		  fakeTickDataTimerTask.removeChangeListener(this);
		  try {
				fakeTickDataTimerTask.join();
		  } catch (InterruptedException ex) {
				Logger.getLogger(TradingSimulation1.class.getName()).log(Level.SEVERE, null, ex);
		  }

		  Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
		  while (positionIterator.hasNext()) {
				PositionA position = positionIterator.next();
				if (position.isOpen()) {
					 Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
					 while (transactionIterator.hasNext()) {
						  Transaction transaction = transactionIterator.next();
						  if (transaction.isOpen() && transaction.quantity > 0) {
								transaction.id_status = Transaction.TRANSACTION_STATUS_CANCELED;
								transaction.timestamp = lastTick.getDate();
						  }
						  if (transaction.isOpen() && transaction.quantity < 0) {
								transaction.price = lastTick.getPrice();
								transaction.timestamp = lastTick.getDate();
						  }
					 }

					 logger.info("close position at " + lastTick.getDate() + " " + lastTick.getPrice());
				}
		  }
	 }

	 private void writePosition(Position position) {
		  Quotes.getInstance().writePosition("simulation", position);

            Iterator<Transaction> transactionIterator=position.getTransactionHashMap().values().iterator();
            while (transactionIterator.hasNext()) Quotes.getInstance().writeTransaction("simulation", transactionIterator.next());
	 }

	 private void openNewPosition(Date timestamp, String id_position_type, Float underlyingEntry, Float underlyingStop) {
		  long id_position = Quotes.getInstance().getNextPositionID("simulation");

		  logger.info("open new position at " + timestamp + " " + underlyingEntry);

		  PositionA position = new PositionA(id_account, id_position, chartParameters.getInstrument(), underlyingStop, underlyingEntry, 0f);

		  position.isin = "FFFFFF";
		  position.id_position_type = id_position_type;

		  id_transaction++;
		  Transaction transaction = new Transaction(id_position, String.valueOf(id_transaction), Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, timestamp, underlyingEntry, 0f, 1L);
		  position.getTransactionHashMap().put(transaction.id_transaction, transaction);
		  positionHashMap.put(id_position, position);

		  writePosition(position);
	 }

	 private void processPositions(Tick tick) {
		  Iterator<PositionA> positionIterator = positionHashMap.values().iterator();
		  while (positionIterator.hasNext()) {
				PositionA position = positionIterator.next();

				if (position.isOpen()) {
					 Iterator<Transaction> transactionIterator = position.getTransactionHashMap().values().iterator();
					 while (transactionIterator.hasNext()) {
						  Transaction transaction = transactionIterator.next();
						  if (transaction.isOpen() && transaction.quantity > 0 && ((position.isLong() && transaction.price <= tick.getPrice()) || (position.isShort() && transaction.price >= tick.getPrice()))) {
								transaction.id_status = Transaction.TRANSACTION_STATUS_EXECUTED;
								transaction.price = tick.getPrice();
								transaction.timestamp = tick.getDateTime().toDate();

								position.buy(transaction.price, transaction.quantity);
								logger.info("execute pruchase transaction " + transaction.getStatusDescription());

								//@todo: adjust SL
								id_transaction++;
								Transaction transactionSL = new Transaction(position.id_position, String.valueOf(id_transaction), Transaction.TRANSACTION_STATUS_OPEN, Transaction.TRANSACTION_TYPE_LIMITED, tick.getDateTime().toDate(), position.underlyingStopLoss, 0f, -position.quantity);
								position.getTransactionHashMap().put(transactionSL.id_transaction, transactionSL);

								writePosition(position);

						  } else if (transaction.isOpen() && transaction.quantity < 0 && ((position.isLong() && transaction.price >= tick.getPrice()) || (position.isShort() && transaction.price <= tick.getPrice()))) {
								transaction.id_status = Transaction.TRANSACTION_STATUS_EXECUTED;
								transaction.price = tick.getPrice();
								transaction.timestamp = tick.getDateTime().toDate();

								position.sell(transaction.price, transaction.quantity);

								writePosition(position);

								logger.info("execute sale transaction " + transaction.getStatusDescription());
						  }
					 }
				}
		  }
	 }
}

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
package com.manticore.database;

import com.manticore.foundation.Position;
import com.manticore.foundation.Tick;
import com.manticore.foundation.StockExchange;
import com.manticore.foundation.Transaction;
import com.manticore.foundation.Instrument;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.*;
import java.util.ArrayList;
import java.io.FileReader;
import javax.swing.tree.DefaultMutableTreeNode;
import org.joda.time.format.DateTimeFormat;

import au.com.bytecode.opencsv.CSVReader;
import com.manticore.foundation.PositionDataStorage;
import com.manticore.foundation.TanReader;
import com.manticore.util.Settings;
import com.manticore.foundation.TimeMarker;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.DriverManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import org.joda.time.format.DateTimeFormatter;

public class Quotes implements PositionDataStorage, TanReader {

    public final static DateTimeFormatter SQL_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private static Quotes instance = null;
    private Connection connection;
    public final static int DB_XMARKETS = 3;

    public Quotes() {
        String connectionStr = Settings.getInstance().get("manticore-trader", "Quotes", "connectionUrlStr");
        String homeDir = System.getProperty("user.home");
        if (!homeDir.endsWith(File.separator)) {
            homeDir += File.separator;
        }
        String connectionUrl = connectionStr.replace("${user.home}", homeDir);

        String className = Settings.getInstance().get("manticore-trader", "Quotes", "className");
        String username = Settings.getInstance().get("manticore-trader", "Quotes", "username");
        String password = Settings.getInstance().get("manticore-trader", "Quotes", "password");
        boolean loggedIn = false;


        if (connectionUrl.length() > 0 && className.length() > 0 && username.length() > 0) {
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            //props.setProperty("ssl","true");
            props.setProperty("data compression", "true");
            props.setProperty("compress", "true");
            try {
                connection = DriverManager.getConnection(connectionUrl, props);
                loggedIn = !connection.isClosed();
            } catch (SQLException ex) {
                Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!loggedIn) {
            LoginDialog l = new LoginDialog();
            connection = l.getConnection();
            try {
                connection.setAutoCommit(true);
                connection.setCatalog("public");
            } catch (SQLException ex) {
                Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Quotes(String classname, String hostStr, String login, String password) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(hostStr, login, password);
            connection.setAutoCommit(true);
            connection.setCatalog("public");
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static Quotes logIn(String classname, String hostStr, String login, String password) {
        instance = new Quotes(classname, hostStr, login, password);
        return instance;
    }

    public boolean importInstrumentTickData(long id_instrument, long id_stock_exchange) {
        boolean newDataFound = false;
        try {
            newDataFound = importInstrumentTickData(id_instrument, id_stock_exchange, getImportType(id_instrument, id_stock_exchange));
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newDataFound;
    }

    public boolean importInstrumentTickData(long id_instrument, long id_stock_exchange, int id_import_type) {
        boolean newDataFound = false;

        Logger.getLogger("QuotesConnection").finest("start import for instrument " + id_instrument + " at exchange " + id_stock_exchange);
        //Logger.getLogger("QuotesConnection").setLevel(Level.FINEST);
        try {
            if (id_import_type == 1) {
                newDataFound = importFromUrl1(id_instrument, id_stock_exchange);
            } else if (id_import_type == 2) {
                newDataFound = importFromUrl2(id_instrument, id_stock_exchange);
            } else if (id_import_type == 3) {
                newDataFound = importFromUrl3(id_instrument, id_stock_exchange);
            } else if (id_import_type == 4) {
                newDataFound = importFromUrl4(id_instrument, id_stock_exchange);
            } else if (id_import_type == 5) {
                newDataFound = importFromUrl5(id_instrument, id_stock_exchange);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newDataFound;
    }

    public void logOff() {
        if (instance != null) {
            try {
                instance.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                instance = null;
            }
        }
    }

    public static Quotes getInstance() {


        if (instance == null) {
            instance = new Quotes();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public static int executeUpdate(String sqlStr) {
        int r = 0;
        try {
            Statement statement = getInstance().getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            r = statement.executeUpdate(sqlStr);
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }

    public ArrayList<Interval> getExcludedIntervalArrayList(long id_stock_exchange) {
        ArrayList<Interval> excludedIntervalArrayList = new ArrayList();
        String sqlStr = "SELECT * FROM trader.stock_exchange_excluded_interval WHERE id_stock_exchange=" + id_stock_exchange + ";";

        try {

            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            while (rs.next()) {
                Interval interval = new Interval(new DateTime(rs.getTimestamp("interval_start")), new DateTime(rs.getTimestamp("interval_end")));
                excludedIntervalArrayList.add(interval);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return excludedIntervalArrayList;
    }

    private ArrayList<StockExchange> getStockExchangeArrayListFromSQL(String sqlStr) {
        ArrayList<StockExchange> stockExchangeArrayList = new ArrayList();
        try {

            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            while (rs.next()) {
                long id_stock_exchange = rs.getLong("id_stock_exchange");
                String symbol = rs.getString("symbol");
                String description = rs.getString("description");
                int openingMinute = rs.getInt("opening_minute");
                int closingMinute = rs.getInt("closing_minute");
                ArrayList<Interval> excludedIntervalArrayList = getExcludedIntervalArrayList(id_stock_exchange);
                stockExchangeArrayList.add(new StockExchange(id_stock_exchange, symbol, description, openingMinute, closingMinute, excludedIntervalArrayList));
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Quotes.class.getName()).info(sqlStr);
        }

        return stockExchangeArrayList;
    }

    public ArrayList<StockExchange> getStockExchangeArrayList() {
        String sqlStr = "SELECT * FROM trader.stock_exchange;";
        return getStockExchangeArrayListFromSQL(sqlStr);
    }

    public StockExchange getStockExchange(String symbol) {
        String sqlStr = "SELECT * FROM trader.stock_exchange WHERE symbol='" + symbol + "'";
        return getStockExchangeFromSql(sqlStr);
    }

    public StockExchange getStockExchange(long id_stock_exchange) {
        String sqlStr = "SELECT * FROM trader.stock_exchange WHERE id_stock_exchange=" + id_stock_exchange;
        return getStockExchangeFromSql(sqlStr);
    }

    private StockExchange getStockExchangeFromSql(String sqlStr) {
        StockExchange stockExchange = null;

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            if (rs.next()) {
                long id_stock_exchange = rs.getLong("id_stock_exchange");
                String symbol = rs.getString("symbol");
                String description = rs.getString("description");
                int openingMinute = rs.getInt("opening_minute");
                int closingMinute = rs.getInt("closing_minute");
                ArrayList<Interval> excludedIntervalArrayList = getExcludedIntervalArrayList(id_stock_exchange);
                stockExchange = new StockExchange(id_stock_exchange, symbol, description, openingMinute, closingMinute, excludedIntervalArrayList);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Quotes.class.getName()).info(sqlStr);
        }

        return stockExchange;
    }

    public ArrayList<Instrument> getInstrumentArrayListFromSQL(String sqlStr) {
        ArrayList<Instrument> instrumentArrayList = new ArrayList();

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            while (rs.next()) {
                Instrument instrument = new Instrument();
                instrument.setId(rs.getLong("id_instrument"));
                instrument.setSymbol(rs.getString("symbol"));
                instrument.setName(rs.getString("description"));
                instrument.setId_currency(rs.getLong("id_instrument_currency"));

                String sqlStr2 = new StringBuffer().append("Select id_ext_key, value from trader.ext_key_instrument where id_instrument=").append(instrument.getId()).append(";").toString();

                instrument.getKeyHashMap().putAll(getKeyHashMap(sqlStr2, "id_ext_key", "value"));
                instrument.setStockExchangeArrayList(getStockExchangeArrayListFromInstrumentID(instrument.getId()));
                instrumentArrayList.add(instrument);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Quotes.class.getName()).info(sqlStr);
        }

        return instrumentArrayList;
    }

    public ArrayList<Instrument> getInstrumentArrayList(long id_stock_exchange) {
        String sqlStr = "SELECT t1.* FROM trader.instrument t1 INNER JOIN trader.stock_exchange_instrument t2 ON (t1.id_instrument=t2.id_instrument) WHERE t2.id_stock_exchange=" + id_stock_exchange + ";";
        return getInstrumentArrayListFromSQL(sqlStr);
    }

    public ArrayList<Instrument> getInstrumentArrayListFromIndexID(long id_instrument_index) {
        String sqlStr = "select t2.* from trader.instrument_index t1 inner join trader.instrument t2 ON (t1.id_instrument=t2.id_instrument) where t1.id_instrument_index=" + id_instrument_index + ";";
        return getInstrumentArrayListFromSQL(sqlStr);
    }

    public ArrayList<Instrument> getInstrumentArrayList() {
        String sqlStr = "SELECT * FROM trader.instrument;";
        return getInstrumentArrayListFromSQL(sqlStr);
    }

    public ArrayList<Instrument> getInstrumentArrayList(String description) {
        String sqlStr = "SELECT * FROM trader.instrument WHERE description like '" + description + "';";
        return getInstrumentArrayListFromSQL(sqlStr);
    }

    public ArrayList<Instrument> getIndexInstrumentsArrayList() {
        String sqlStr = "SELECT * FROM trader.instrument  WHERE id_instrument_type='I';";
        return getInstrumentArrayListFromSQL(sqlStr);
    }

    public Float getInstrumentIndexRation(long id_instrument, long id_instrument_index) {
        Float ratio = null;
        String sqlStr = "SELECT ratio FROM trader.instrument_index  WHERE id_instrument=" + id_instrument + " AND id_instrument_index=" + id_instrument_index + ";";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            if (rs.next()) {
                ratio = rs.getFloat("ratio");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ratio;
    }

    private Instrument getInstrumentFromSQL(String sqlStr) {
        Instrument instrument = null;

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            while (rs.next()) {
                instrument = new Instrument();
                instrument.setId(rs.getLong("id_instrument"));
                instrument.setSymbol(rs.getString("symbol"));
                instrument.setName(rs.getString("description"));
                instrument.setId_currency(rs.getLong("id_instrument_currency"));

                String sqlStr2 = new StringBuffer().append("Select id_ext_key, value from trader.ext_key_instrument where id_instrument=").append(instrument.getId()).append(";").toString();

                instrument.getKeyHashMap().putAll(getKeyHashMap(sqlStr2, "id_ext_key", "value"));
                instrument.setStockExchangeArrayList(getStockExchangeArrayListFromInstrumentID(instrument.getId()));
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return instrument;
    }

    public Instrument getInstrumentFromSymbol(String symbol) {
        String sqlStr = "SELECT * FROM trader.instrument WHERE symbol ='" + symbol + "';";
        return getInstrumentFromSQL(sqlStr);
    }

    public Instrument getInstrumentFromID(long id_instrument) {
        String sqlStr = "SELECT * FROM trader.instrument WHERE id_instrument =" + id_instrument + ";";
        return getInstrumentFromSQL(sqlStr);
    }

    public void addInstrumentToIndexInstrument(long id_instrument_index, long id_instrument) {
        String sqlStr = "INSERT INTO trader.instrument_index (id_instrument,id_instrument_index) VALUES (" + id_instrument + "," + id_instrument_index + "); ";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.execute(sqlStr);
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeInstrumentFromIndexInstrument(long id_instrument_index, long id_instrument) {
        String sqlStr = "DELETE FROM trader.instrument_index WHERE id_instrument=" + id_instrument + " AND id_instrument_index=" + id_instrument_index + "; ";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.execute(sqlStr);
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public DefaultMutableTreeNode getInstrumentTreeNode() {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        String sqlStr = "SELECT * FROM trader.instrument WHERE id_instrument_type in ('I','C','P') ORDER BY id_instrument_type";

        Iterator<Instrument> indexInstrumentIterator = getInstrumentArrayListFromSQL(sqlStr).iterator();
        while (indexInstrumentIterator.hasNext()) {
            Instrument indexInstrument = indexInstrumentIterator.next();
            DefaultMutableTreeNode indexInstrumentNode = new DefaultMutableTreeNode(indexInstrument, true);

            String sqlStr2 = new StringBuffer().append("select ").append("t2.* ").append("from trader.instrument_index t1 ").append("INNER JOIN trader.instrument t2 on (t1.id_instrument=t2.id_instrument) ").append("where t1.id_instrument_index=" + indexInstrument.getId() + " ").append("order by description ASC ").append("; ").toString();

            Iterator<Instrument> instrumentIterator = getInstrumentArrayListFromSQL(sqlStr2).iterator();
            while (instrumentIterator.hasNext()) {
                indexInstrumentNode.add(new DefaultMutableTreeNode(instrumentIterator.next(), false));
            }

            rootNode.add(indexInstrumentNode);
        }

        return rootNode;
    }

    public ArrayList<StockExchange> getStockExchangeArrayListFromInstrumentID(long id_instrument) {
        String sqlStr = "select  t2.* from trader.stock_exchange_instrument t1 inner join trader.stock_exchange t2 ON (t1.id_stock_exchange=t2.id_stock_exchange) where t1.id_instrument=" + id_instrument + "; ";
        return getStockExchangeArrayListFromSQL(sqlStr);
    }

    private static String getSQLDate(DateTime dateTime) {
        return SQL_DATE_TIME_FORMATTER.print(dateTime);
    }

    private static String getSQLDate(Date date) {
        return getSQLDate(new DateTime(date));
    }

    private static DateTime parseSQLDateTime(String s) {
        return SQL_DATE_TIME_FORMATTER.parseDateTime(s);
    }

    private static Date parseSQLDate(String s) {
        return parseSQLDateTime(s).toDate();
    }

    public ResultSet getTickdataResultSet(
            long id_instrument, long id_stock_exchange, Date dateTimeFrom, Date dateTimeTo) throws SQLException {

        String sqlStr = new StringBuffer().append("select ").append("t1.id_instrument, ").append("t1.id_stock_exchange, ").append("t1.\"timestamp\", ").append("t1.price, ").append("coalesce(t2.quantity,0) quantity ").append("from trader.tickdata t1 ").append("left join trader.volumedata t2 ON ").append("( ").append("   t1.id_instrument=t2.id_instrument AND t1.id_stock_exchange=t2.id_stock_exchange AND  t1.\"timestamp\"=t2.\"timestamp\" ").append(") ").append("where t1.id_instrument=").append(id_instrument).append(" AND t1.id_stock_exchange=").append(id_stock_exchange).append(" AND t1.\"timestamp\">='").append(getSQLDate(dateTimeFrom)).append("' ").append(" AND t1.\"timestamp\"<='").append(getSQLDate(dateTimeTo)).append("' ").append("ORDER BY \"timestamp\" ASC;").toString();

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sqlStr);
        return statement.executeQuery(sqlStr);
    }

    public ResultSet getEODResultSet(
            String isin, Date dateTimeFrom, Date dateTimeTo) throws SQLException {

        String sqlStr = "SELECT * FROM quotes_eod WHERE isin='" + isin + "'";
        sqlStr +=
                " AND \"day\">='" + getSQLDate(dateTimeFrom) + "'";
        sqlStr +=
                " AND \"day\"<='" + getSQLDate(dateTimeTo) + "'";
        sqlStr +=
                "ORDER BY \"day\" ASC;";

        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sqlStr);
        return statement.executeQuery(sqlStr);
    }

    public void importFromCSV(String filename) throws FileNotFoundException, IOException, SQLException {
        long line = 0;
        String sql = "INSERT INTO quote VALUES ";
        StringBuffer strb = new StringBuffer(sql);

        CSVReader reader = new CSVReader(new FileReader(filename), ';', '\"', 2);
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (line > 0) {
                strb.append(", ");
            }

            strb.append("(20735, ").append(nextLine[1]).append(", ").append(nextLine[0]).append(", ").append(nextLine[2]).append(", ").append(nextLine[4]).append(", ").append(nextLine[6]).append(", ").append(nextLine[8]).append(") ");
            line++;

        }


        strb.append(";");
        int r = connection.createStatement().executeUpdate(strb.toString());
    }

    public String getIsin(long id_instrument) {
        return getExtKeyInstrument(5, id_instrument);
    }

    public Date getLastTimeStamp(long id_instrument, long id_stock_exchange) {
        Date last = null;

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            String sqlstr = " select " + " max(\"timestamp\") " + " from trader.tickdata " + " where id_instrument=" + id_instrument + " and id_stock_exchange=" + id_stock_exchange + " ; ";
            ResultSet rs = statement.executeQuery(sqlstr);
            if (rs.next()) {
                last = rs.getTimestamp(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return last;
    }

    public void importTickdata() {

        try {
            String sqlstr = "select t1.id_instrument, t1.id_stock_exchange, t1.id_import_type, t2.description, t3.description from trader.stock_exchange_instrument t1 INNER JOIN trader.instrument t2 ON (t1.id_instrument=t2.id_instrument) INNER JOIN trader.stock_exchange t3 ON(t1.id_stock_exchange=t3.id_stock_exchange);";
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlstr);
            while (rs.next()) {
                long id_instrument = rs.getLong("id_instrument");
                long id_stock_exchange = rs.getLong("id_stock_exchange");
                int id_import_type = rs.getInt("id_import_type");
                String instrument_description = rs.getString(4);
                String stock_exchange_description = rs.getString(5);

                String msg = new StringBuffer().append("import new quotes for ").append(instrument_description).append(" at ").append(stock_exchange_description).append(": ").append(importInstrumentTickData(id_instrument, id_stock_exchange, id_import_type)).toString();
                Logger.getAnonymousLogger().info(msg);
            }
            rs.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public int getImportType(long id_instrument, long id_stock_exchange) throws SQLException {
        int import_type = 1;

        String sqlstr = "SELECT id_import_type FROM trader.stock_exchange_instrument WHERE id_instrument=" + id_instrument + " AND id_stock_exchange=" + id_stock_exchange + ";";
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(sqlstr);
        if (rs.next()) {
            import_type = rs.getInt("id_import_type");
        }

        return import_type;
    }

    public boolean importFromUrl1(long id_instrument, long id_stock_exchange) throws FileNotFoundException, IOException, SQLException, URISyntaxException {
        String id = getExtKeyInstrument(4, id_instrument);

        String urlString = "http://tools.godmode-trader.de/omniOmniQuotes.php?&id=" + id + "&exchanges_id=" + id_stock_exchange + "&quote_source=last#tools.godmode-trader.de#";
        //"http://tools.godmode-trader.de/German30/omniquotes.php?&id=" + id_instrument + "&exchanges_id=" + id_stock_exchange + "&quote_source=lastMillis";

        String day = "";

        return importTickData(id_instrument, id_stock_exchange, urlString, day);
    }

    public boolean importFromUrl2(long id_instrument, long id_stock_exchange) throws FileNotFoundException, IOException, SQLException, URISyntaxException {
        HashMap<Long, String> tickHashMap = new HashMap<Long, String>();
        HashMap<Long, String> volumeHashMap = new HashMap<Long, String>();

        String ID_NOTATION = getExtKeyInstrument(2, id_instrument, id_stock_exchange);
        ///mdgtools.mdgms.com/prices/history_list.csv?BLOCKSIZE=ALL&CODE_TYPE_RESOLUTION=1m&FORMAT=0&ID_GROUP_TYPE_PRICE=1!9!25&ID_NOTATION=20735&ID_QUALITY_PRICE=4&LANG=de&OFFSET_END_RANGE=0&OFFSET_START_RANGE=0&SORT=DATETIME_FIRST&VERSION=2&XID=32373b1b-4bc8dddf-5a50146d12d5255c /prices/history_list.csv?BLOCKSIZE=99999&CODE_RESOLUTION=1D&CODE_TYPE_OFFSET=TD&FORMAT=0&ID_NOTATION=20735&ID_QUALITY_PRICE=4&LANG=de&OFFSET=0&OFFSET_END_RANGE=0&OFFSET_START_RANGE=1&VERSION=2&XID=32373b1b-4bc8dddf-5a50146d12d5255c
        String longUrlStr = "http://mdgtools.mdgms.com/prices/history_list.csv?BLOCKSIZE=ALL&CODE_TYPE_RESOLUTION=1m&FORMAT=0&ID_GROUP_TYPE_PRICE=1&ID_QUALITY_PRICE=4&ID_TYPE_OFFSET=M&LANG=de&OFFSET_END_RANGE=0&OFFSET_START_RANGE=120&VERSION=2&XID=" + XID.getXID() + "&ID_NOTATION=" + ID_NOTATION;
        String shortUrlStr = "http://mdgtools.mdgms.com/prices/history_list.csv?BLOCKSIZE=ALL&CODE_TYPE_RESOLUTION=1m&FORMAT=0&ID_GROUP_TYPE_PRICE=1!9!25&ID_QUALITY_PRICE=4&LANG=de&OFFSET_END_RANGE=0&VERSION=2&XID=" + XID.getXID() + "&ID_NOTATION=" + ID_NOTATION;

        long line = 0;


        Date last = getLastTimeStamp(id_instrument, id_stock_exchange);
        Long lastMillis = last != null ? last.getTime() : 0L;

        // intraday: use shortUrl, else use longUrl
        Duration duration = new Duration(new DateTime(lastMillis), new DateTime(DateTimeZone.forID("Europe/Berlin")));
        URL url = (duration.getMillis() > 12L * 60L * 60L * 1000L) ? new URL(longUrlStr) : new URL(shortUrlStr);

        CSVReader reader = new CSVReader(new InputStreamReader(url.openConnection().getInputStream()), ';', '\"', 2);
        String[] nextLine;


        while ((nextLine = reader.readNext()) != null) {
            Long firstMillis = 0L;
            try {
                firstMillis = NumberFormat.getIntegerInstance().parse(nextLine[1]).longValue();
                firstMillis *= 1000L;
            } catch (ParseException ex) {
                Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (firstMillis > lastMillis) {
                String tickValue = new StringBuffer().append("(").append(id_instrument).append(", ").append(id_stock_exchange).append(", to_timestamp('").append(nextLine[1]).append("') + interval '1 minute', ").append(nextLine[2]).append(") ").toString();
                String volumeValue = new StringBuffer().append("(").append(id_instrument).append(", ").append(id_stock_exchange).append(", to_timestamp('").append(nextLine[1]).append("') + interval '1 minute', ").append(nextLine[8]).append(") ").toString();

                tickHashMap.put(firstMillis, tickValue);
                volumeHashMap.put(firstMillis, volumeValue);
            }
        }

        StringBuffer strb = new StringBuffer("INSERT INTO trader.tickdata VALUES ");
        Iterator<String> iterator = tickHashMap.values().iterator();
        line = 0;
        while (iterator.hasNext()) {
            strb.append(line > 0 ? ", " : "").append(iterator.next());
            line++;
        }
        strb.append(";");
        writeTicksToDatabase(strb.toString(), line, id_instrument, id_stock_exchange);

        strb = new StringBuffer("INSERT INTO trader.volumedata VALUES ");
        iterator = volumeHashMap.values().iterator();
        line = 0;
        while (iterator.hasNext()) {
            strb.append(line > 0 ? ", " : "").append(iterator.next());
            line++;
        }
        strb.append(";");
        return writeTicksToDatabase(strb.toString(), line, id_instrument, id_stock_exchange);
    }

    public boolean importFromUrl3(long id_instrument, long id_stock_exchange) throws FileNotFoundException, IOException, SQLException, URISyntaxException {
        String isin = getExtKeyInstrument(5, id_instrument);

        String urlString = "http://tools.boerse-go.de/index-tool/omniquotes.php?&exchanges_id=" + id_stock_exchange + "&identifier_type=isin&identifier=" + isin + "&quote_source=last";
        String day = "";

        return importTickData(id_instrument, id_stock_exchange, urlString, day);
    }

    public boolean importFromUrl4(long id_instrument, long id_stock_exchange) throws FileNotFoundException, IOException, SQLException, URISyntaxException {
        String id = getExtKeyInstrument(4, id_instrument);

        String urlString = "http://tools.boerse-go.de/rohstoffe/omniquotes.php?&id=" + id + "&quote_source=last";
        String day = "";

        return importTickData(id_instrument, id_stock_exchange, urlString, day);
    }

    // Currencies
    public boolean importFromUrl5(long id_instrument, long id_stock_exchange) throws FileNotFoundException, IOException, SQLException, URISyntaxException {
        String id = getExtKeyInstrument(4, id_instrument);
        String urlString = "http://tools.godmode-trader.de/omniOmniQuotes.php?id=" + id + "&exchanges_id=" + id_stock_exchange + "&quote_source=bid";
        String day = "";

        return importTickData(id_instrument, id_stock_exchange, urlString, day);
    }

    private boolean importTickData(long id_instrument, long id_stock_exchange, String urlString, String day) throws SQLException, MalformedURLException, IOException {
        HashMap<String, String> tickHashMap = new HashMap<String, String>();

        MutableDateTime currentDate = new MutableDateTime(DateTimeZone.forID("Europe/Berlin"));
        int dateShift = 0;

        if (currentDate.getDayOfWeek() == DateTimeConstants.TUESDAY
                || currentDate.getDayOfWeek() == DateTimeConstants.WEDNESDAY
                || currentDate.getDayOfWeek() == DateTimeConstants.THURSDAY
                || currentDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {

            if (currentDate.getMinuteOfDay() < getStockExchange(id_stock_exchange).getOpeningMinute()) {
                currentDate.addDays(-1);
            }
        } else if (currentDate.getDayOfWeek() == DateTimeConstants.SATURDAY) {
            currentDate.addDays(-1);
        } else if (currentDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            currentDate.addDays(-2);
        } else if (currentDate.getDayOfWeek() == DateTimeConstants.MONDAY) {
            if (currentDate.getMinuteOfDay() < getStockExchange(id_stock_exchange).getOpeningMinute()) {
                currentDate.addDays(-3);
            }
        }

        //@todo: add support for holidays

        //@todo: test, if data are a duplicate of the day before

        Date first = null;
        long line = 0;


        Date last = getLastTimeStamp(id_instrument, id_stock_exchange);

        Settings.setProxy();

        CSVReader reader = new CSVReader(new InputStreamReader(new URL(urlString).openConnection().getInputStream()), ';', '\"');
        String[] nextLine;

        String lastTimestampStr = "";

        while ((nextLine = reader.readNext()) != null) {
            if (line > 0 && nextLine.length == 2 && day != null) {
                try {
                    first = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").parseDateTime(day + " " + nextLine[1]).toDate();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not parse date", ex);
                }
            }
            if (line > 0 && nextLine.length == 2 && (last == null || first.after(last))) {
                lastTimestampStr = day + " " + nextLine[1];

                String key = day + " " + nextLine[1];
                String value = new StringBuffer().append("(").append(id_instrument).append(", ").append(id_stock_exchange).append(", '").append(day).append(" ").append(nextLine[1]).append("', ").append(nextLine[0]).append(") ").toString();

                tickHashMap.put(key, value);

                line++;
            }
            if (line == 0 && day.length() == 0) {
                //day = nextLine[0];
                day = DateTimeFormat.forPattern("yyyy-MM-dd").print(currentDate);
                line++;
            }
        }

        StringBuilder strb = new StringBuilder("INSERT INTO trader.tickdata VALUES ");

        Iterator<String> iterator = tickHashMap.values().iterator();
        line = 0;
        while (iterator.hasNext()) {
            strb.append(line > 0 ? ", " : "").append(iterator.next());
            line++;
        }

        strb.append(";");

        //test if lastMillis timestamp is after current date --> dismiss old data
        if (lastTimestampStr.length() == 0 || SQL_DATE_TIME_FORMATTER.parseDateTime(lastTimestampStr).isBeforeNow()) {
            return writeTicksToDatabase(strb.toString(), line, id_instrument, id_stock_exchange);
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.FINEST, "last timestamp {0} is after now. Dismiss old data.", lastTimestampStr);
            return false;
        }
    }

    private boolean writeTicksToDatabase(String sqlStr, long line, long id_instrument, long id_stock_exchange) {
        int r = 0;

        if (line > 1) {
            try {
                Statement statement = connection.createStatement();
                r = statement.executeUpdate(sqlStr);
            } catch (SQLException ex) {
                Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0} records added", r);
        }
        return r > 0;
    }

    public String getExtKeyInstrument(int id_ext_key, long id_instrument, long id_stock_exchange) {
        String key = "";
        String sqlstr = "SELECT value FROM trader.ext_key_stock_exchange_instrument WHERE id_ext_key=" + id_ext_key + " AND id_instrument=" + id_instrument + " AND id_stock_exchange=" + id_stock_exchange + ";";

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlstr);

            if (rs.next()) {
                key = rs.getString(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return key;
    }

    public String getExtKeyInstrument(int id_ext_key, long id_instrument) {
        String key = "";
        String sqlstr = "SELECT value FROM trader.ext_key_instrument WHERE id_ext_key=" + id_ext_key + " AND id_instrument=" + id_instrument + ";";

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlstr);

            if (rs.next()) {
                key = rs.getString(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return key;
    }

    public HashMap<String, String> getKeyHashMap(String sqlStr, String keyFieldId, String valueFieldId) {
        HashMap<String, String> hashMap = new HashMap<String, String>();

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);
            while (rs.next()) {
                hashMap.put(rs.getString(keyFieldId), rs.getString(valueFieldId));
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hashMap;
    }

    public ArrayList<Tick> getQuotesTickArrayList(long id_instrument, long id_stock_exchange, DateTime dateTimeFrom) {
        ArrayList<Tick> tickArrayList = new ArrayList();
        String key = "";
        String sqlstr = "select "
                + "t1.id_instrument, "
                + "t1.id_stock_exchange, "
                + "t1.\"timestamp\", "
                + "t1.price, "
                + "coalesce(t2.quantity,0) quantity "
                + "from trader.tickdata t1"
                + "left join trader.volumedata t2 ON "
                + "( "
                + "   t1.id_instrument=t2.id_instrument AND t1.id_stock_exchange=t2.id_stock_exchange AND  t1.\"timestamp\"=t2.\"timestamp\" "
                + ") "
                + " where id_instrument= " + id_stock_exchange
                + " and id_stock_exchange= " + id_instrument
                + " AND \"timestamp\">=timestamp '" + getSQLDate(dateTimeFrom) + "' ORDER BY \"timestamp\" ASC;";
        ResultSet rs = null;

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(sqlstr);

            while (rs.next()) {
                Tick tick = new Tick(rs.getTimestamp("timestamp"), rs.getFloat("price"), rs.getLong("quantity"));
                tickArrayList.add(tick);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tickArrayList;
    }

    public boolean hasQuantity(long id_instrument, long id_stock_exchange) {
        boolean hasQuantity = false;
        String sqlstr = "SELECT has_quantity FROM trader.stock_exchange_instrument WHERE id_instrument=" + id_instrument + " AND id_stock_exchange=" + id_stock_exchange + ";";

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlstr);

            if (rs.next()) {
                hasQuantity = rs.getBoolean("has_quantity");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);

        }

        return hasQuantity;
    }

    public ArrayList<Transaction> getTransactionArrayList(long id_instrument, DateTime dateTimeFrom, DateTime dateTimeTo) {
        ArrayList<Transaction> transactionArrayList = new ArrayList();
        String sqlStr = new StringBuffer().append("select ").append("	id_transaction ").append("from ").append("	trader.transaction t1 ").append("	inner join trader.position t2 on t1.id_position=t2.id_position ").append("where ").append("	id_instrument=").append(id_instrument).append("	and id_status='X' ").append("	and timestamp>='").append(getSQLDate(dateTimeFrom)).append("' ").append("	and timestamp<='").append(getSQLDate(dateTimeTo)).append("' ").append(" order by timestamp ASC;").toString();
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);

            while (rs.next()) {
                Transaction transaction = readTransaction(rs.getString("id_transaction"));
                transactionArrayList.add(transaction);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(this.getClass().getName()).warning(sqlStr);
        }

        return transactionArrayList;
    }

    public void insertTimeMarker(DateTime dateTime, String shortDescription, String longDescription) {
        StringBuffer sqlStrB = new StringBuffer().append("INSERT INTO trader.time_marker (\"timestamp\", short_description, long_description) ").append("VALUES ('").append(getSQLDate(dateTime)).append("'").append(", '").append(shortDescription).append("'").append(", '").append(longDescription).append("');");

        Quotes.executeUpdate(sqlStrB.toString());
    }

    public ArrayList<TimeMarker> getTimeMarkerArrayList(DateTime dateTimeFrom, DateTime dateTimeTo) {
        ArrayList<TimeMarker> timeMarkerArrayList = new ArrayList();

        String sqlStr = "select * from trader.time_marker WHERE \"timestamp\">='" + getSQLDate(dateTimeFrom) + "' AND \"timestamp\"<='" + getSQLDate(dateTimeTo) + "' ORDER BY \"timestamp\" ASC;";

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);

            while (rs.next()) {
                DateTime timestamp = new DateTime(rs.getTimestamp("timestamp"));
                String shortDescription = rs.getString("short_description");
                String longDescription = rs.getString("long_description");

                TimeMarker timeMarker = new TimeMarker(timestamp, shortDescription, longDescription);
                timeMarkerArrayList.add(timeMarker);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Quotes.class.getName()).info(sqlStr);
        }

        return timeMarkerArrayList;
    }

    public ArrayList<String> getIsinArrayList() {
        ArrayList<String> isinArrayList = new ArrayList();
        String sqlStr = "select "
                + "value "
                + "from trader.instrument t1"
                + "INNER JOIN trader.ext_key_instrument t2 ON (t1.id_instrument=t2.id_instrument) "
                + "WHERE id_ext_key=5 "
                + "; ";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = statement.executeQuery(sqlStr);

            while (rs.next()) {
                isinArrayList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(Quotes.class.getName()).info(sqlStr);
        }

        return isinArrayList;
    }

    @Override
    public void writePosition(String schema, Position position) {
        boolean writeAsUpdate = false;


        String sqlStr = "SELECT * FROM " + schema + ".transaction where id_position=" + position.id_position + (";");
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            writeAsUpdate = resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder stringBuffer = new StringBuilder();
        if (writeAsUpdate) {
            stringBuffer.append("UPDATE ").append(schema).append(".position ").append(" SET (id_position_status, quantity, average_entry, profit, stop_loss, entry, take_profit) = ('").append(position.getPositionStatus()).append("', ").append(position.quantity).append(", ").append(position.averageEntry).append(", ").append(position.profit.floatValue()).append(", ").append(position.underlyingStopLoss).append(", ").append(position.underlyingEntry).append(", ").append(position.underlyingTarget).append(") ").append("WHERE id_position=").append(position.id_position).append(";");
        } else {
            stringBuffer.append("INSERT INTO ").append(schema).append(".position ").append("(id_account, id_position, id_instrument, id_position_type, id_position_status, isin, quantity, average_entry, profit, stop_loss, entry, take_profit) VALUES (").append(position.id_account).append(", ").append(position.id_position).append(", ").append(position.instrument.id_instrument).append(", '").append(position.id_position_type).append("', '").append(position.getPositionStatus()).append("', '").append(position.getIsin()).append("', ").append(position.quantity).append(", ").append(position.averageEntry).append(", ").append(position.profit.floatValue()).append(", ").append(position.underlyingStopLoss).append(", ").append(position.underlyingEntry).append(", ").append(position.underlyingTarget).append(");");
        }
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            int r = statement.executeUpdate(stringBuffer.toString());
            Logger.getLogger(getClass().getName()).log(Level.FINER, "Successfully wrote {0} position(s).", r);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(getClass().getName()).warning(stringBuffer.toString());
        }
    }

    @Override
    public void writeTransaction(String schema, Transaction transaction) {
        boolean writeAsUpdate = false;


        String sqlStr = "SELECT * FROM " + schema + ".transaction where id_transaction=" + transaction.id_transaction + (";");
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            writeAsUpdate = resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        StringBuilder stringBuffer = new StringBuilder();
        if (writeAsUpdate) {

            // an update should oocur only, when there was either an cancellation or and execution
            // so we update only the values which depend on the execution
            // @todo: WRONG! stop loss order will be adjusted
            stringBuffer.append("UPDATE ").append(schema).append(".transaction ").append(" SET (quantity, price, fee, timestamp, id_status) = (").append(transaction.quantity).append(", ").append(transaction.price).append(", ").append(transaction.fee).append(", '").append(getSQLDate(transaction.timestamp)).append("', '").append(transaction.id_status).append("') ").append("WHERE id_transaction=").append(transaction.id_transaction).append(";");
        } else {
            stringBuffer.append("INSERT INTO ").append(schema).append(".transaction ").append("(id_position, id_transaction, id_transaction_type, quantity, price, fee, timestamp, id_status, underlying_stop, underlying_entry, underlying_target) ").append(" VALUES (").append(transaction.id_position).append(", '").append(transaction.id_transaction).append("', '").append(transaction.id_transaction_type).append("', ").append(transaction.quantity).append(", ").append(transaction.price).append(", ").append(transaction.fee).append(", '").append(getSQLDate(transaction.timestamp)).append("', '").append(transaction.id_status).append("', ").append(transaction.underlying_stop).append(", ").append(transaction.underlying_entry).append(", ").append(transaction.underlying_target).append(");");
        }

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            int r = statement.executeUpdate(stringBuffer.toString());
            Logger.getLogger(getClass().getName()).log(Level.FINER, "Successfully wrote {0}transaction(s).", r);
        } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(getClass().getName()).warning(stringBuffer.toString());
        }
    }

    @Override
    public long getNextPositionID(String schema) {
        long id_position = 0;
        String sqlStr = "select nextval('" + schema + ".position_id_position_seq');";
        try {

            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            if (resultSet.next()) {
                id_position = resultSet.getLong(1);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id_position;
    }

    public String getNewPositionID(String schema) {
        String id_position = java.util.UUID.randomUUID().toString();
        return id_position;
    }

    @Override
    public Position readPosition(long id_position) {
        Position position = null;
        String sqlStr = "SELECT * FROM trader.position where id_position=" + id_position + (";");
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            if (resultSet.next()) {
                long id_account = resultSet.getLong("id_account");
                Instrument instrument = getInstrumentFromID(resultSet.getLong("id_instrument"));


                position = new Position(id_account, id_position, instrument);
                position.id_position_type = resultSet.getString("id_position_type");
                position.quantity = resultSet.getLong("quantity");
                position.averageEntry = resultSet.getFloat("average_price");
                position.profit = resultSet.getFloat("profit");

                position.underlyingStopLoss = resultSet.getFloat("stop_loss");
                position.underlyingEntry = resultSet.getFloat("entry");
                position.underlyingTarget = resultSet.getFloat("take_profit");

                position.isin = resultSet.getString("isin");
                position.setTransactionHashMap(getTransactionHashMapFromPositionID(position.id_position));
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }

        return position;
    }

    @Override
    public ArrayList<Position> getPositionArrayList(boolean openPositionsOnly) {
        ArrayList<Position> positionArrayList = new ArrayList<Position>();

        String sqlStr = openPositionsOnly
                ? "select * from trader.position where quantity>0 or id_position in (SELECT DISTINCT id_position FROM trader.transaction WHERE id_status='O') order by id_position;"
                : "select * from trader.position where id_position in (SELECT DISTINCT id_position FROM trader.transaction WHERE id_status='X' or id_status='O') order by id_position;";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            while (resultSet.next()) {
                long id_account = resultSet.getLong("id_account");
                long id_position = resultSet.getLong("id_position");
                Instrument instrument = getInstrumentFromID(resultSet.getLong("id_instrument"));


                Position position = new Position(id_account, id_position, instrument);
                position.id_position_type = resultSet.getString("id_position_type");
                position.quantity = resultSet.getLong("quantity");
                position.averageEntry = resultSet.getFloat("average_entry");
                position.profit = resultSet.getFloat("profit");
                position.underlyingStopLoss = resultSet.getFloat("stop_loss");
                position.underlyingEntry = resultSet.getFloat("entry");
                position.underlyingTarget = resultSet.getFloat("take_profit");

                position.isin = resultSet.getString("isin");
                position.setTransactionHashMap(getTransactionHashMapFromPositionID(position.id_position));

                positionArrayList.add(position);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return positionArrayList;
    }

    @Override
    public Transaction readTransaction(String id_transaction) {
        Transaction transaction = null;
        String sqlStr = "SELECT * FROM trader.transaction where id_transaction=" + id_transaction + (";");

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            if (resultSet.next()) {

                long id_position = resultSet.getLong("id_position");
                Long quantity = resultSet.getLong("quantity");
                Float price = resultSet.getFloat("price");
                Float fee = resultSet.getFloat("fee");
                String id_status = resultSet.getString("id_status");
                String id_transaction_type = resultSet.getString("id_transaction_type");

                // if order is executed we should see a timestamp
                Date timestamp = null;
                if (id_status.equals(Transaction.TRANSACTION_STATUS_EXECUTED)) {
                    timestamp = resultSet.getTimestamp("timestamp");
                }
                transaction = new Transaction(id_position, id_transaction, id_status, id_transaction_type, timestamp, price, fee, quantity);

                transaction.underlying_stop = resultSet.getFloat("underlying_stop");
                transaction.underlying_entry = resultSet.getFloat("underlying_entry");
                transaction.underlying_target = resultSet.getFloat("underlying_target");
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return transaction;
    }

    @Override
    public HashMap<String, Transaction> getTransactionHashMapFromPositionID(long id_position) {
        HashMap<String, Transaction> transactionArrayList = new HashMap<String, Transaction>();
        String sqlStr = "SELECT * FROM trader.transaction where id_position=" + id_position + (";");

        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            while (resultSet.next()) {
                String id_transaction = resultSet.getString("id_transaction");
                Long quantity = resultSet.getLong("quantity");
                Float price = resultSet.getFloat("price");
                Float fee = resultSet.getFloat("fee");
                String id_status = resultSet.getString("id_status");
                String id_transaction_type = resultSet.getString("id_transaction_type");

                // if order is executed we should see a timestamp
                Date timestamp = null;
                if (id_status.equals(Transaction.TRANSACTION_STATUS_EXECUTED)) {
                    timestamp = resultSet.getTimestamp("timestamp");
                }
                Transaction transaction = new Transaction(id_position, id_transaction, id_status, id_transaction_type, timestamp, price, fee, quantity);

                transaction.underlying_stop = resultSet.getFloat("underlying_stop");
                transaction.underlying_entry = resultSet.getFloat("underlying_entry");
                transaction.underlying_target = resultSet.getFloat("underlying_target");

                transactionArrayList.put(transaction.id_transaction, transaction);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return transactionArrayList;
    }

    @Override
    public String getTan(long id_account, String key) {
        String tan = "";
        String sqlStr = new StringBuffer().append("SELECT id_tan_card, value FROM trader.tan WHERE valid=true AND \"key\"='").append(key).append("' AND id_tan_card=(SELECT max(id_tan_card) from trader.tan_card WHERE id_account=").append(String.valueOf(id_account)).append(");").toString();

        Statement statement;
        try {
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            if (resultSet.next()) {
                long id_tan_card = resultSet.getLong("id_tan_card");
                tan = resultSet.getString("value");

                String sqlStr2 = new StringBuffer().append("UPDATE trader.tan SET valid=false WHERE id_tan_card=").append(String.valueOf(id_tan_card)).append(" AND ").append(" \"key\"='").append(key).append("';").toString();

                statement.executeUpdate(sqlStr2);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tan;
    }

    public void executeSqlBatch(String sqlBatchScript) {
        int r = 0;
        Statement statement;

        StringTokenizer tokenizer = new StringTokenizer(sqlBatchScript, ";");
        while (tokenizer.hasMoreTokens()) {
            String sqlStr = tokenizer.nextToken().trim().concat(";");
            Logger.getAnonymousLogger().log(Level.FINEST, sqlStr);

            try {
                if (sqlStr.length() > 3) {
                    statement = getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    statement.executeUpdate(sqlStr);
                    Logger.getAnonymousLogger().log(Level.INFO, "updated {0} records", statement.getUpdateCount());
                    statement.close();
                    r++;
                }

            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            getConnection().commit();
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "{0} updates applied successfully.", r);
    }

    public ResultSet getResultSet(String sqlStr) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(sqlStr);
    }

    public long getNextPositionID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writePosition(Position arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeTransaction(Transaction arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getPositionProfits() {
        StringBuilder stringBuilder = new StringBuilder("0");

        String sqlStr = new StringBuilder().append(" select ").append("	profit ").append("	, a1 ").append("	, profit*100/a1 ").append(" from trader.position NATURAL JOIN ( ").append("	select ").append("		ID_POSITION ").append("		, sum(QUANTITY*price) as a1 ").append("	from trader.TRANSACTION ").append("	where QUANTITY>0 ").append("	group by ID_POSITION ").append("	) ").append(" where profit<>0 ").toString();

        //String sqlStr = "select profit from trader.position where profit<>0;";
        try {
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = statement.executeQuery(sqlStr);
            while (resultSet.next()) {
                stringBuilder.append(",").append(resultSet.getDouble(3));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Quotes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return stringBuilder.toString();
    }
}


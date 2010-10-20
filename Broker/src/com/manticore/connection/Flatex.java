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
package com.manticore.connection;

import com.manticore.foundation.MessageDialog;
import com.manticore.foundation.TanReader;
import com.manticore.foundation.Transaction;
import com.manticore.parser.WebsiteParser;
import com.manticore.parser.WebsiteParser.Site;
import com.manticore.util.HttpClientFactory;
import com.manticore.util.Settings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class Flatex implements ActionListener {

    public final static long TIMER_PERIOD = 900000L;
    public final static int MAX_PRICE_LOOP = 3;
    public final static int MAX_ORDER_LOOP = 3;
    public final static String ORDER_BUY = "0";
    public final static String ORDER_SELL = "1";
    public final static String LIMIT_NONE = "0";
    public final static String LIMIT_LIMIT = "1";
    public final static String LIMIT_STOP_MARKET = "2";
    public final static String LIMIT_STOP_LIMIT = "3";
    public final static String EXTENSION_NONE = "0";
    public final static String EXTENSION_IOC = "1"; //partial intermediate execution or cancel
    public final static String EXTENSION_FOK = "2"; //complete intermediate execution or cancel
    private static Flatex instance = null;
    private DefaultHttpClient client;
    private String login;
    private String password;
    private String password2;
    private String sessionID;
    private Timer timer;
    private TanReader tanReader;
    private static boolean updating = false;
    private static boolean locked = false;
    private final static Logger logger = Logger.getLogger(Flatex.class.getName());

    private Flatex(TanReader tanReader) throws Exception {
        updating = true;

        MessageDialog.getInstance().showAndLock("connect to Flatex, please wait...\n");

        this.tanReader = tanReader;

        login = Settings.getInstance().get("manticore-trader", "Flatex", "accountID");
        password = Settings.getInstance().get("manticore-trader", "Flatex", "password");
        password2 = Settings.getInstance().get("manticore-trader", "Flatex", "tradingPassword");

        if (login.length() == 0 || password.length() == 0 || password2.length() == 0) {
            AccessDialog accessDialog = new AccessDialog();

            if (accessDialog.isLoggedIn()) {
                login = accessDialog.getAccountID();
                password = accessDialog.getPassword();
                password2 = accessDialog.getTradingPassword();
            } else {
                throw new Exception("No credentials available");
            }
        }


        client = HttpClientFactory.getClient();
        HttpClientParams.setRedirecting(client.getParams(), false);

        WebsiteParser.getInstance().getSite("FlatexLoginForm", client);

        Iterator<Cookie> iterator = client.getCookieStore().getCookies().iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (cookie.getName().equals("JSESSIONID")) {
                sessionID = cookie.getValue();
                MessageDialog.getInstance().showAndLock("Session ID is " + sessionID +"\n");
            }
        }

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("userId.text", new StringBody(login));
        reqEntity.addPart("pin.text", new StringBody(password));
        reqEntity.addPart("pin.text", new StringBody(password));
        reqEntity.addPart("loginButton.x", new StringBody("33"));
        reqEntity.addPart("loginButton.y", new StringBody("12"));
        reqEntity.addPart("inPopup.checked", new StringBody("0"));
        reqEntity.addPart("useSessionPassword.checked", new StringBody("true"));
        reqEntity.addPart("popup", new StringBody("true"));

        String[][] parameterArray = {{"sessionID", sessionID}, {"suffix", ""}};
        WebsiteParser.getInstance().getSiteFromPost("FlatexLoginForm2", parameterArray, client, reqEntity);

        //<td class="FormAreaInputFieldLeftLabel">Eingabe TAN: G6 D6 F2</td>
        Site site = WebsiteParser.getInstance().getSite("FlatexTanForm", client);
        if (site.hasNextNode()) {
            String tanKey = site.getString("tan").replace(" ", "");
            String tan = tanReader.getTan(1, tanKey);

            if (tan.length() == 0 & !(tanReader instanceof TanDialog)) {
                TanDialog tanDialog = new TanDialog();
                tan = tanDialog.getTan(1, tanKey);
            }
            if (tan.length() > 0) {
                reqEntity = new MultipartEntity();

                reqEntity.addPart("sessionPasswordEditField.text", new StringBody(tan));
                reqEntity.addPart("sessionPasswordButton.x", new StringBody("33"));
                reqEntity.addPart("sessionPasswordButton.y", new StringBody("12"));

                String location = WebsiteParser.getInstance().getHeaderValueFromPost("FlatexTanForm2", client, reqEntity, "Location");
                if (location.equalsIgnoreCase("https://konto.flatex.de/onlinebanking-flatex/overviewFormAction.do?method=refresh")) {
                    MessageDialog.getInstance().release("Tan is ok\nReady.\n");
                } else {
                    MessageDialog.getInstance().release("Problems with TAN\nYou can not order yet!");
                }
            }
        }
        timer = new Timer("FlatexConnectionTimer", true);
        timer.schedule(new KeepAlive(), TIMER_PERIOD, TIMER_PERIOD);
        updating = false;
    }

    public Flatex() throws Exception {
        updating = true;

        login = Settings.getInstance().get("manticore-trader", "Flatex", "accountID");
        password = Settings.getInstance().get("manticore-trader", "Flatex", "password");
        password2 = Settings.getInstance().get("manticore-trader", "Flatex", "tradingPassword");

        if (login.length() == 0 || password.length() == 0 || password2.length() == 0) {
            AccessDialog accessDialog = new AccessDialog();

            if (accessDialog.isLoggedIn()) {
                login = accessDialog.getAccountID();
                password = accessDialog.getPassword();
                password2 = accessDialog.getTradingPassword();
            } else {
                throw new Exception("No credentials available");
            }
        }

        Logger.getLogger(Flatex.class.getName()).info("open new Flatex connection");

        client = HttpClientFactory.getClient();
        HttpClientParams.setRedirecting(client.getParams(), false);

        WebsiteParser.getInstance().getSite("FlatexLoginForm", client);

        Iterator<Cookie> iterator = client.getCookieStore().getCookies().iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (cookie.getName().equals("JSESSIONID")) {
                sessionID = cookie.getValue();
                System.out.println("session ID " + sessionID);
            }
        }

        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("userId.text", new StringBody(login));
        reqEntity.addPart("pin.text", new StringBody(password));
        reqEntity.addPart("pin.text", new StringBody(password));
        reqEntity.addPart("loginButton.x", new StringBody("33"));
        reqEntity.addPart("loginButton.y", new StringBody("12"));
        reqEntity.addPart("inPopup.checked", new StringBody("0"));
        reqEntity.addPart("useSessionPassword.checked", new StringBody("false"));
        reqEntity.addPart("popup", new StringBody("true"));

        String[][] parameterArray = {{"sessionID", sessionID}, {"suffix", ""}};
        WebsiteParser.getInstance().getSiteFromPost("FlatexLoginForm2", parameterArray, client, reqEntity);

        timer = new Timer("FlatexConnectionTimer", true);
        timer.schedule(new KeepAlive(), TIMER_PERIOD, TIMER_PERIOD);

        updating = false;
    }

    public void close() {
        client.getConnectionManager().shutdown();
        client = null;
        timer.cancel();
        instance = null;
    }

    public boolean isSessionEnded() {
        return !ping();
    }

    public static Flatex getInstance(TanReader tanReader) {
        if (instance == null) {
            try {
                instance = new Flatex(tanReader);
            } catch (Exception ex) {
                Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    public Float getAvailableAmount() {
        Float availableAmount = null;
        updating = true;
        String[][] parameterArray = {{"sessionID", sessionID}, {"suffix", ""}};
        Site site = WebsiteParser.getInstance().getSite("FlatexDepotOverview", parameterArray, client);
        if (site.hasNextNode()) {
            availableAmount = site.getFloat("availableAmount");
        }
        updating = false;
        return availableAmount;
    }

    public boolean searchPaper(String key) {
        boolean valid = true;
        updating = true;

        WebsiteParser.getInstance().getSite("FlatexOrderInitialize", client);
        WebsiteParser.getInstance().getSite("FlatexOrderPopulate", "isin", key, client);
        Site site = WebsiteParser.getInstance().getSite("FlatexOrderSearchPaper", "isin", key, client);
        if (site.hasNextNode()) {
            valid = "Trading-Passwort".equalsIgnoreCase(site.getString("TradingPassword"));
        }
        updating = false;
        return valid;
    }

    private DecimalFormat getDecimalFormat() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN);
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setGroupingUsed(false);

        return decimalFormat;
    }

    private DecimalFormat getIntegerFormat() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getIntegerInstance(Locale.GERMAN);
        decimalFormat.setMaximumFractionDigits(0);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }

    public String orderLimit(String orderType, Long quantity, String limitType, Float limit, Float stop, String extension) {
        String orderID = "";
        String quantityStr = getIntegerFormat().format(quantity);
        String limitStr = limitType.equals(LIMIT_LIMIT) || limitType.equals(LIMIT_STOP_LIMIT) ? getDecimalFormat().format(limit) : null;
        String stopStr = limitType.equals(LIMIT_STOP_MARKET) || limitType.equals(LIMIT_STOP_LIMIT) ? getDecimalFormat().format(stop) : null;
        Logger.getLogger(Flatex.class.getName()).info("---\n" + "Ordertype: " + orderType + "\nQuantity: " + quantityStr + "\nLimit: " + limitStr + "\nStop:" + stopStr + "\n---");
        updating = true;

        try {
            MultipartEntity reqEntity;
            reqEntity = new MultipartEntity();
            reqEntity.addPart("buySell.selectedItemIndex", new StringBody(orderType));
            reqEntity.addPart("quantity.text", new StringBody(quantityStr));
            reqEntity.addPart("tradeType.checked", new StringBody("1"));
            reqEntity.addPart("limitType.selectedItemIndex", new StringBody(limitType));
            if (limitType.equals(LIMIT_LIMIT) || limitType.equals(LIMIT_STOP_LIMIT)) {
                reqEntity.addPart("limit.text", new StringBody(limitStr));
            }
            if (limitType.equals(LIMIT_STOP_MARKET) || limitType.equals(LIMIT_STOP_LIMIT)) {
                reqEntity.addPart("stopValue.text", new StringBody(stopStr));
            }
            if (limitType.equals(LIMIT_LIMIT) || limitType.equals(LIMIT_NONE)) {
                reqEntity.addPart("limitExtension.selectedItemIndex", new StringBody(extension));
            }
            reqEntity.addPart("validityType.selectedItemIndex", new StringBody("0"));
            reqEntity.addPart("nextButton.x", new StringBody("63"));
            reqEntity.addPart("nextButton.y", new StringBody("11"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);
            reqEntity = new MultipartEntity();
            reqEntity.addPart("buySell.selectedItemIndex", new StringBody(orderType));
            reqEntity.addPart("quantity.text", new StringBody(quantityStr));
            reqEntity.addPart("tradeType.checked", new StringBody("1"));
            reqEntity.addPart("limitType.selectedItemIndex", new StringBody(limitType));
            if (limitType.equals(LIMIT_LIMIT) || limitType.equals(LIMIT_STOP_LIMIT)) {
                reqEntity.addPart("limit.text", new StringBody(limitStr));
            }
            if (limitType.equals(LIMIT_STOP_MARKET) || limitType.equals(LIMIT_STOP_LIMIT)) {
                reqEntity.addPart("stopValue.text", new StringBody(stopStr));
            }
            if (limitType.equals(LIMIT_LIMIT) || limitType.equals(LIMIT_NONE)) {
                reqEntity.addPart("limitExtension.selectedItemIndex", new StringBody(extension));
            }
            reqEntity.addPart("validityType.selectedItemIndex", new StringBody("0"));
            reqEntity.addPart("nextButton.x", new StringBody("63"));
            reqEntity.addPart("nextButton.y", new StringBody("11"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);
            reqEntity = new MultipartEntity();
            reqEntity.addPart("nextButton.x", new StringBody("52"));
            reqEntity.addPart("nextButton.y", new StringBody("11"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);

            Site site = WebsiteParser.getInstance().getSite("FlatexOrderResponse", client);
            if (site.hasNextNode()) {
                orderID = site.getString("id_transaction");
            }
            logger.info("made order " + orderID);
            updating = false;

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
        }
        return orderID;
    }

    public String getOrderStatus(String orderID) throws IOException, Exception {
        String status = "";
        updating = true;
        WebsiteParser.getInstance().getSite("FlatexOrderListInitialize", client);
        Site site = WebsiteParser.getInstance().getSite("FlatexOrderListIndex", client);
        while (site.hasNextNode()) {
            if (site.getString("id_transacion").contains(orderID)) {
                status = site.getString("status");
            }
        }
        updating = false;
        return status;
    }

    public int getOrderIndex(String orderID) {
        int index = -1;
        updating = true;
        WebsiteParser.getInstance().getSite("FlatexOrderListInitialize", client);
        index = WebsiteParser.getInstance().getSite("FlatexOrderListIndex", client).getRowIndex("id_transaction", orderID);
        updating = false;
        return index;
    }

    public void cancelOrder(String orderID) {
        try {
            int index = getOrderIndex(orderID);
            updating = true;
            WebsiteParser.getInstance().getSite("FlatexOrderCancelList", "RowIndex", String.valueOf(index), client);
            WebsiteParser.getInstance().getSite("FlatexOrderCancelInitialize", client);

            MultipartEntity reqEntity = new MultipartEntity();
            reqEntity.addPart("nextButton.x", new StringBody("52"));
            reqEntity.addPart("nextButton.y", new StringBody("11"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderCancelForm", client, reqEntity);

            WebsiteParser.getInstance().getSite("FlatexOrderCancelInitialize2", client);

            reqEntity = new MultipartEntity();
            reqEntity.addPart("paperOrderListButton.x", new StringBody("52"));
            reqEntity.addPart("paperOrderListButton.y", new StringBody("11"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderCancelForm", client, reqEntity);
            WebsiteParser.getInstance().getSite("FlatexOrderListInitialize", client);

            updating = false;

        } catch (IOException ex) {
            Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void adjustSLOrder(String orderID, Float price, Integer quantity)  {
        int index = getOrderIndex(orderID);
        updating = true;
        try {
            WebsiteParser.getInstance().getSite("FlatexOrderModifyList", "RowIndex", String.valueOf(index), client);
            WebsiteParser.getInstance().getSite("FlatexOrderModifyInitialize", client);
            MultipartEntity reqEntity = new MultipartEntity();
            String quantityStr = getIntegerFormat().format(quantity.intValue());
            reqEntity.addPart("quantity.text", new StringBody(quantityStr));
            String priceString = getDecimalFormat().format(price.floatValue());
            reqEntity.addPart("stopValue.text", new StringBody(priceString));
            reqEntity.addPart("validityType.selectedItemIndex", new StringBody("2"));
            String dateStr = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(new Date());
            reqEntity.addPart("validityDate.text", new StringBody(dateStr));
            reqEntity.addPart("nextButton.x", new StringBody("87"));
            reqEntity.addPart("nextButton.y", new StringBody("19"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderModifyForm", client, reqEntity);
            reqEntity = new MultipartEntity();
            reqEntity.addPart("nextButton.x", new StringBody("87"));
            reqEntity.addPart("nextButton.y", new StringBody("19"));
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderModifyForm", client, reqEntity);
            WebsiteParser.getInstance().getSiteFromPost("FlatexOrderModifyForm", client, reqEntity);
            WebsiteParser.getInstance().getSite("FlatexOrderListInitialize", client);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
        }
        updating = false;
    }

    public Transaction getTransaction(long id_position, String id_transaction) {
        Transaction transaction = null;
        updating = true;

        int index = getOrderIndex(id_transaction);

        WebsiteParser.getInstance().getSite("FlatexOrderDetailsPrepare", "RowIndex", String.valueOf(index), client);
        Site site = WebsiteParser.getInstance().getSite("FlatexOrderDetails", client);

        if (site.hasNextNode()) {
            id_transaction=site.getString("id_transaction");

            String id_status = Transaction.translateStatus(site.getString("statusKey"));
            String id_transaction_type = Transaction.translateTransactionType(site.getString("transactionTypeKey"));

            //Execution time not always shown!
            // use order time otherwise
            Date timestamp = site.getDateTime("executionTimestamp");
            if (timestamp==null) timestamp = site.getDateTime("timestamp");

            //Execution price not always shown!
            // use Limit or Stop otherwise
            Float price = site.getFloat("executionPrice");
            if (price == 0f) {
                price = site.getFloat("limitPrice");
            }
            if (price == 0f) {
                price = site.getFloat("stopPrice");
            }
            //Fee is null, if there is no fee.
            Float fee = site.getFloat("fee");
            if (fee==null) fee=0f;

            Long quantity = Transaction.translatesQuantity(site.getString("quantityKey"), site.getLong("quantity"));
            transaction = new Transaction(id_position, id_transaction, id_status, id_transaction_type, timestamp, price, fee, quantity);
        }

        updating = false;
        return transaction;
    }

    public String orderDirect(long id_position, String orderType, Long quantity, Float limit, Float stop) {
        String quantityStr = getIntegerFormat().format(quantity);
        Transaction transaction = null;


        updating = true;
        for (int orderLoop = 0; orderLoop < MAX_ORDER_LOOP; orderLoop++) {
            try {
                logger.info("start order loop " + orderLoop);
                MultipartEntity reqEntity = new MultipartEntity();
                reqEntity.addPart("buySell.selectedItemIndex", new StringBody(orderType));
                reqEntity.addPart("quantity.text", new StringBody(quantityStr));
                reqEntity.addPart("tradeType.checked", new StringBody("0"));
                reqEntity.addPart("nextButton.x", new StringBody("12"));
                reqEntity.addPart("nextButton.y", new StringBody("12"));
                WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);
                WebsiteParser.getInstance().getSite("FlatexOrderFormTimer1", client);

                for (int priceLoop = 0; priceLoop < MAX_PRICE_LOOP && transaction == null; priceLoop++) {
                    logger.info("start price loop " + priceLoop);
                    float quote = 0f;
                    for (int i = 0; i < 2; i++) {
                        Site site = WebsiteParser.getInstance().getSite("FlatexOrderFormTimer2", client);
                        if (site.hasNextNode()) {
                            quote = site.getFloat("quote");
                        }
                    }
                    logger.info("found price " + quote);

                    if (quote > 0f && (limit == null || quote < limit) && (stop == null || stop < quote)) {
                        priceLoop = MAX_PRICE_LOOP;
                        logger.info("price " + quote + " is between limit and stop (" + limit + "; " + stop + ") ");
                        logger.info("will make a transaction and do not ask for another price");

                        reqEntity = new MultipartEntity();
                        reqEntity.addPart("orderButton.x", new StringBody("27"));
                        reqEntity.addPart("orderButton.y", new StringBody("13"));
                        Site site = WebsiteParser.getInstance().getSiteFromPost("FlatexOrderResponseDirect", client, reqEntity);
                        if (site.hasNextNode()) {
                            String id_transaction = site.getString("id_transaction");
                            String id_status = Transaction.translateStatus(site.getString("statusKey"));
                            String id_transaction_type = Transaction.translateTransactionType(site.getString("transactionTypeKey"));
                            Date timestamp = site.getDateTime("timestamp");
                            Float price = site.getFloat("limitPrice");
                            Float fee = 0f;
                            quantity = Transaction.translatesQuantity(site.getString("quantityKey"), site.getLong("quantity"));
                            transaction = new Transaction(id_position, id_transaction, id_status, id_transaction_type, timestamp, price, fee, quantity);
                            logger.info("made transaction: " + transaction.getDescription());

                        }
                        if (transaction.id_status.equals(Transaction.TRANSACTION_STATUS_EXECUTED) || transaction.id_status.equals(Transaction.TRANSACTION_STATUS_OPEN)) {
                            orderLoop = MAX_ORDER_LOOP;
                        }

                    } else {
                        logger.info("No price found or price " + quote + " is not between limit and stop (" + limit + "; " + stop + ") ");
                        logger.info("will not make a transaction, but ask for new price after 4 seconds");
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        reqEntity = new MultipartEntity();
                        reqEntity.addPart("newPriceButton.x", new StringBody("89"));
                        reqEntity.addPart("newPriceButton.y", new StringBody("15"));
                        WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);
                    }
                }

                if (transaction != null && transaction.id_status.equals(Transaction.TRANSACTION_STATUS_CANCELED)) {
                    logger.info("transaction found, but was denied.");
                    logger.info("will make another transaction");
                    reqEntity = new MultipartEntity();
                    if (orderType.equals(ORDER_BUY)) {
                        reqEntity.addPart("sameBuyOrderButton.x", new StringBody("124"));
                        reqEntity.addPart("sameBuyOrderButton.y", new StringBody("14"));
                    } else {
                        reqEntity.addPart("sameSellOrderButton.x", new StringBody("124"));
                        reqEntity.addPart("sameSellOrderButton.y", new StringBody("14"));
                    }
                    WebsiteParser.getInstance().getSiteFromPost("FlatexOrderForm", client, reqEntity);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Flatex.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        updating = false;

        return transaction != null ? transaction.id_transaction : "";
    }

    public boolean ping() {
        updating = true;
        String siteContentString = WebsiteParser.getInstance().getSiteContentString("FlatexPing", "sessionID", sessionID, client);
        boolean valid = siteContentString.trim().equalsIgnoreCase("ok");
        if (valid) {
            logger.info("sent a ping in order to keep connection alive.");
        } else {
            logger.warning("Close connection, when ping was not successful.");
            close();
        }
        updating = false;
        return valid;
    }

    /**
     * @return the sessionID
     */
    public String getSessionID() {
        return sessionID;
    }

    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    /**
     * @return the locked
     */
    public static boolean isLocked() {
        return locked || updating;
    }

    /**
     * @param locked the locked to set
     */
    public static void setLocked(boolean l) {
        locked = l;
    }

    public static boolean lock() {
        boolean valid = !isLocked();
        locked = true;

        return valid;
    }

    public static void unlock() {
        locked = false;
    }

    private class KeepAlive extends TimerTask {

        @Override
        public void run() {
            if (!updating) {
                ping();
            }
        }
    }
}

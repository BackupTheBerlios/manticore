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
package com.manticore.chart;

import com.manticore.database.Quotes;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import com.manticore.swingui.FormatedTextField;
import com.manticore.swingui.GridBagPane;

public class TransactionDialog extends JDialog implements ActionListener, KeyListener {
    private static final Pattern SHARES_PATTERN = Pattern.compile("Stück/Nominal\\s*([\\d\\.,]+)\\s*Stk.", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    private static final Pattern PRICE_PATTERN = Pattern.compile("Limit\\s*([\\d\\.,]+)\\s*EUR", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    private static final Pattern ID_PATTERN = Pattern.compile("Ordernummer\\s*([A-Za-z\\d]+)", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("Eingereicht am\\s*([\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d\\:\\d\\d\\:\\d\\d]+)\\s", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    private static final Pattern TRANSACTION_TYPE_PATTERN = Pattern.compile("Geschäftsart\\s*([A-Za-z]+)\\s", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);
    private static final Pattern ASSET_TYPE_PATTERN = Pattern.compile("DEUT\\.BANK WXXL([CP]) DAX", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);

    private static final String CALL_STR="Call";
    private static final String PUT_STR="Put";

    FormatedTextField jTextField_TransactionID;
    FormatedTextField jTextField_Fee;
    FormatedTextField jTextField_Shares;
    FormatedTextField jTextField_MarketPrice;
    FormatedTextField jTextField_Amount;
    FormatedTextField jTextField_Timestamp;
    JComboBox jComboBox_Type;
    long symbol_id;

    public TransactionDialog(Component parentComponent, long symbol_id, double fee, double shares, double marketPrice) throws ParseException {
        this.symbol_id = symbol_id;

        JDialog dialog = new JDialog();
        dialog.setTitle((shares < 0) ? "Sell Shares:" : "Bye Shares");
        dialog.setResizable(false);
        dialog.setLocationByPlatform(true);

        GridBagPane gridBagPane = new GridBagPane();

        jTextField_TransactionID = new FormatedTextField(0, FormatedTextField.INTEGER_FORMAT, true);
        jTextField_Fee = new FormatedTextField(fee, FormatedTextField.DECIMAL_FORMAT, true, this);
        jTextField_Shares = new FormatedTextField(shares, FormatedTextField.INTEGER_FORMAT, true, this);
        jTextField_MarketPrice = new FormatedTextField(marketPrice, FormatedTextField.DECIMAL_FORMAT, true, this);
        jTextField_Amount = new FormatedTextField(0.00, FormatedTextField.DECIMAL_FORMAT, false);
        jTextField_Timestamp=new FormatedTextField(new Date(), FormatedTextField.DATETIME_FORMAT, false);

        jComboBox_Type=new JComboBox();
        jComboBox_Type.setEditable(false);
        jComboBox_Type.setLightWeightPopupEnabled(true);
        jComboBox_Type.addItem(CALL_STR);
        jComboBox_Type.addItem(PUT_STR);

        parseClipboardContents();
        
        calculateAmount();

        JButton saveButton = new JButton("save");
        saveButton.setDefaultCapable(true);
        saveButton.addActionListener(this);

        gridBagPane.add(jTextField_TransactionID, "label=Transaction ID:, weightx=0.5f, fill=HORIZONTAL");
        gridBagPane.add(jComboBox_Type, "nl, label=Type:, weightx=1f, gridwidth=2, fill=HORIZONTAL");
        gridBagPane.add(jTextField_Timestamp, "nl, label=Timestamp:, weightx=1f, gridwidth=2, fill=HORIZONTAL");
        gridBagPane.add(jTextField_Fee, "nl, label=Fee:, weighx=0.5f, fill=HORIZONTAL");
        gridBagPane.add(jTextField_Shares, "nl, label=Shares/Price:, weightx=0.5f, fill=HORIZONTAL");
        gridBagPane.add(jTextField_MarketPrice, "weightx=0.5f, fill=HORIZONTAL");
        gridBagPane.add(jTextField_Amount, "nl, label=Amount:, gridx=3, weightx=0.5f, fill=HORIZONTAL");
        gridBagPane.add(saveButton, "nl, gridx=3, weightx=0.0f, fill=HORIZONTAL");

        dialog.setLayout(new FlowLayout(FlowLayout.LEADING));
        dialog.add(gridBagPane);
        dialog.pack();
        
        dialog.setLocationByPlatform(true);
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setVisible(true);


    }

    public int getShares() throws ParseException {
        return jTextField_Shares.getIntegerValue();
    }

    public double getAmount() throws ParseException {
        return jTextField_Amount.getDoubleValue();
    }

    public double getMarketPrice() throws ParseException {
        return jTextField_MarketPrice.getDoubleValue();
    }

    private void calculateAmount() throws ParseException {
        jTextField_Amount.setValue(-(jTextField_Shares.getDoubleValue() * jTextField_MarketPrice.getDoubleValue()) - jTextField_Fee.getDoubleValue());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            calculateAmount();

            StringBuffer sqlStr = new StringBuffer();

            String dateStr=DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print( new DateTime(jTextField_Timestamp.getDateTimeValue()) );

            String type=jComboBox_Type.getSelectedItem().equals(CALL_STR) ? "C" : "P";

            sqlStr.append("INSERT INTO transaction (id_transaction, id_symbol, type,  shares, price, fee, amount, up_date) ").append("VALUES (").append(jTextField_TransactionID.getLongValue()).append(", ").append(symbol_id).append(", '").append(type).append("', ").append(jTextField_Shares.getIntegerValue()).append(", ").append(jTextField_MarketPrice.getDoubleValue()).append(", ").append(jTextField_Fee.getDoubleValue()).append(", ").append(jTextField_Amount.getDoubleValue()).append(", '").append( dateStr ).append("');");
            Logger.getAnonymousLogger().finest(sqlStr.toString());
            Quotes.executeUpdate(sqlStr.toString());
            setVisible(false);

        } catch (ParseException ex) {
            Logger.getLogger(TransactionDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            calculateAmount();
        } catch (ParseException ex) {
            Logger.getLogger(TransactionDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the String residing on the clipboard.
     *
     * @return any text found on the Clipboard; if none found, return an
     * empty String.
     */
    private String getClipboardContents() {
        String result = "";
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);
        boolean hasTransferableText =
          (contents != null) &&
          contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        if (hasTransferableText) {
            try {
                result = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
                System.out.println(ex);
                ex.printStackTrace();
            } catch (IOException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    private void parseClipboardContents() {
        String sign="";
        String contentStr=getClipboardContents();
        
        if (contentStr.length()>0) {
            Matcher m = TIMESTAMP_PATTERN.matcher(contentStr);
            if (m.find()) {
                try {
                    jTextField_Timestamp.parseDateValue(m.group(1), DateFormat.MEDIUM, Locale.GERMANY);
                } catch (ParseException ex) {
                    Logger.getLogger(TransactionDialog.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            m = TRANSACTION_TYPE_PATTERN.matcher(contentStr);
            if (m.find() && m.group(1).equalsIgnoreCase("Verkauf")) sign="-";

            m = SHARES_PATTERN.matcher(contentStr);
            if (m.find()) jTextField_Shares.setText(sign + m.group(1));
            
            m = PRICE_PATTERN.matcher(contentStr);
            if (m.find()) jTextField_MarketPrice.setText(m.group(1));

            m = ID_PATTERN.matcher(contentStr);
            if (m.find()) jTextField_TransactionID.setText(m.group(1));
            
            m = ASSET_TYPE_PATTERN.matcher(contentStr);
            if (m.find()) {
                jComboBox_Type.setSelectedItem( m.group(1).equals("C") ? CALL_STR : PUT_STR);
            }
        }
    }
}
    


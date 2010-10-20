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

import com.manticore.connection.Flatex;
import com.manticore.foundation.Transaction;
import com.manticore.parser.WebsiteParser;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class DemoFlatex {
    private final static Pattern TAN_PATTERN = Pattern.compile("([\\s\\w]{8}$)", Pattern.DOTALL | Pattern.UNICODE_CASE);

    public static void main(String args[]) {
        //String s="Eingabe TAN: G6 D6 F2";
        //System.out.println(XMLTools.extractString(s, TAN_PATTERN).replace(" ", ""));
//        try {
//            Flatex connection = Flatex.getInstance(new TanDialog());
//            //        connection.searchPaper("DB03C9");
//            //        try {
//            //            connection.orderLimit(Connection2.ORDER_BUY, 40f, Connection2.LIMIT_LIMIT, 34.23f, null, Connection2.EXTENSION_FOK);
//            //        } catch (UnsupportedEncodingException ex) {
//            //            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
//            //        } catch (IOException ex) {
//            //            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
//            //        } catch (Exception ex) {
//            //            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
//            //        }
//            //connection.getOrderStatus("22966344");
//            //connection.cancelOrder("22975963");
//            System.out.println("Status: " + connection.getTransactipon("27309822").transactionState);
//
//        } catch (IOException ex) {
//            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
//        }
        Flatex flatex;
        try {
            //WebsiteParser.getInstance().writeTempXMLFiles=true;

            flatex = new Flatex();
            Transaction transaction=flatex.getTransaction(0, "28039499");

            System.out.println(transaction.id_transaction);
            System.out.println(transaction.id_transaction_type);
            System.out.println(transaction.id_status);
            System.out.println(transaction.price);
            System.out.println(transaction.quantity);
            System.out.println(transaction.timestamp);
            System.out.println(transaction.fee);
        } catch (Exception ex) {
            Logger.getLogger(DemoFlatex.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}

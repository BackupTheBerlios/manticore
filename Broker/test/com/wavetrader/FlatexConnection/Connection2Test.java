/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wavetrader.FlatexConnection;

import com.manticore.connection.Flatex;
import com.manticore.connection.TanDialog;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author are
 */
public class Connection2Test {
    String orderID="";

    public Connection2Test() {
    }

     /**
     * Test of getInstance method, of class Connection2.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        Flatex result = Flatex.getInstance(new TanDialog());
        assertTrue(result.getSessionID().length()>0);
    }

    /**
     * Test of getAvailableAmount method, of class Connection2.
     */
    @Test
    public void testGetAvailableAmount() {
        System.out.println("getAvailableAmount");
        Float result = Flatex.getInstance(new TanDialog()).getAvailableAmount();
        assertNotNull(result);
        assertTrue(result.floatValue()>0f);
    }

     /**
     * Test of getAvailableAmount method, of class Connection2.
     */
    @Test
    public void testSearchPaper() {
        System.out.println("searchPaper");
        assertTrue(Flatex.getInstance(new TanDialog()).searchPaper("DB03C9"));
    }

     /**
     * Test of getAvailableAmount method, of class Connection2.
     */
    @Test
    public void testOrderLimit() {
        System.out.println("orderLimit");
        orderID = Flatex.getInstance(new TanDialog()).orderLimit(Flatex.ORDER_BUY, 40L, Flatex.LIMIT_LIMIT, 33.23f, null, Flatex.EXTENSION_NONE);
        assertTrue(orderID.length()>0);
    }

    /**
     * Test of getAvailableAmount method, of class Connection2.
     */
    @Test
    public void testGetOrderStatus() {
        System.out.println("getOrderStatus");
        try {
            assertTrue(Flatex.getInstance(new TanDialog()).getOrderStatus(orderID).contains("geroutet"));
        } catch (IOException ex) {
            Logger.getLogger(Connection2Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Connection2Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of getAvailableAmount method, of class Connection2.
     */
    @Test
    public void testCancelOrder() {
        System.out.println("cancelOrder");
        try {

            Flatex.getInstance(new TanDialog()).cancelOrder(orderID);
            assertTrue(Flatex.getInstance(new TanDialog()).getOrderStatus(orderID).contains("gestrichen"));
        } catch (IOException ex) {
            Logger.getLogger(Connection2Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Connection2Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   

}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.manticore.position;

import com.manticore.foundation.Position;
import com.manticore.foundation.PositionDataStorage;
import com.manticore.foundation.Transaction;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author are
 */
public class DefaultPositionDataStorage implements PositionDataStorage {

    public DefaultPositionDataStorage() {
        Logger.getLogger(this.getClass().getName()).info("This is an empty place holder in order to demonstratethe functionality.\nPlease download mantcore-trader from manticore-projects.com for full functionality.");
    }

    @Override
    public long getNextPositionID(String schema) {
        Logger.getLogger(this.getClass().getName()).info("This is an empty place holder in order to demonstratethe functionality.\nPlease download mantcore-trader from manticore-projects.com for full functionality.");
        return 1L;
    }

    public void writePosition(String schema, Position position) {
        Logger.getLogger(this.getClass().getName()).info("This is an empty place holder in order to demonstratethe functionality.\nPlease download mantcore-trader from manticore-projects.com for full functionality.");
    }

    public Position readPosition(long id_position) {
        Position position=null;
        return position;
    }

    public ArrayList<Position> getPositionArrayList(boolean openPositionsOnly) {
        ArrayList<Position> positionArrayList=new ArrayList<Position>();
        return positionArrayList;
    }

    public void writeTransaction(String schema, Transaction transaction) {
        Logger.getLogger(this.getClass().getName()).info("This is an empty place holder in order to demonstratethe functionality.\nPlease download mantcore-trader from manticore-projects.com for full functionality.");
    }

    public Transaction readTransaction(String id_transaction) {
        Transaction transaction=null;
        return transaction;
    }

    public HashMap<String, Transaction> getTransactionHashMapFromPositionID(long id_Position) {
        HashMap<String, Transaction> hashMap=new HashMap<String, Transaction>();
        return hashMap;
    }

}

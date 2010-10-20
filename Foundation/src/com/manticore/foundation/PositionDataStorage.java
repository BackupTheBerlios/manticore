/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.manticore.foundation;

import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author are
 */
public interface PositionDataStorage {

    public long getNextPositionID(String schema);

    public void writePosition(String schema, Position position);

    public Position readPosition(long id_position);

    public abstract ArrayList<Position> getPositionArrayList(boolean openPositionsOnly);

    public void writeTransaction(String schema, Transaction transaction);

    public Transaction readTransaction(String id_transaction);

    public HashMap<String, Transaction> getTransactionHashMapFromPositionID(long id_Position);
}

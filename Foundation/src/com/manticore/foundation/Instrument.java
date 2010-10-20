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
package com.manticore.foundation;

import java.util.HashMap;
import java.util.ArrayList;

public class Instrument implements Comparable<Instrument> {

    public long id_instrument;
    public long id_instrument_currency;
    private String name;
    private String type;
    private String status;
    private HashMap<String, String> keyHashMap;
    private ArrayList<StockExchange> stockExchangeArrayList;

    public Instrument() {
        keyHashMap = new HashMap<String, String>();
        stockExchangeArrayList = new ArrayList<StockExchange>();
    }

    /** Creates a new instance of Symbol */
    public Instrument(String symbol, String name, String wkn, String isin) {
        keyHashMap = new HashMap<String, String>();
        stockExchangeArrayList = new ArrayList<StockExchange>();

        keyHashMap.put("symbol", symbol);
        keyHashMap.put("isin", isin);
        keyHashMap.put("wkn", wkn);

        if (name == null) {
            this.name = symbol;
        }
    }

    public final static Instrument getDefaultInstrument() {
        Instrument instrument = new Instrument();
        instrument.setId(1);
        instrument.setSymbol("DAX");
        instrument.setName("DAX 30");
        instrument.setId_currency(0);

        instrument.setKey("3", "18");
        instrument.setKey("4", "133962");
        instrument.setKey("5", "DE0008469008");
        instrument.setKey("6", "846900");

        return instrument;
    }

    public String toString() {
        String s = getName();
        if (getName() == null) {
            s = getSymbol();
        }
        return s;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id_instrument;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id_instrument = id;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return getKey("symbol");
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        setKey("symbol", symbol);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the wkn
     */
    public String getWkn() {
        return getKey("wkn");
    }

    /**
     * @param wkn the wkn to set
     */
    public void setWkn(String wkn) {
        setKey("wkn", wkn);
    }

    /**
     * @return the isin
     */
    public String getIsin() {
        return getKey("isin");
    }

    /**
     * @param isin the isin to set
     */
    public void setIsin(String isin) {
        setKey("isin", isin);
    }

    @Override
    public int compareTo(Instrument o) {
        return this.name.compareTo(o.getName());
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey(String key) {
        String value = "";

        if (keyHashMap.containsKey(key)) {
            value = keyHashMap.get(key);
        }
        return value;
    }

    public void setKey(String key, String value) {
        if (value != null && key.length() > 0 && value.length() > 0) {
            keyHashMap.put(key, value);
        }
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the keyHashMap
     */
    public HashMap<String, String> getKeyHashMap() {
        return keyHashMap;
    }

    /**
     * @return the stockExchangeArrayList
     */
    public ArrayList<StockExchange> getStockExchangeArrayList() {
        return stockExchangeArrayList;
    }

    /**
     * @param stockExchangeArrayList the stockExchangeArrayList to set
     */
    public void setStockExchangeArrayList(ArrayList<StockExchange> stockExchangeArrayList) {
        this.stockExchangeArrayList = stockExchangeArrayList;
    }

    public boolean isIndex() {
        //@todo: replase these hardcoded ids by testing instrument type
        // which has to be set before when reading the instrument from the database
        return id_instrument == 1 || id_instrument == 2 || id_instrument == 567 || id_instrument == 696;
    }

    /**
     * @return the id_currency
     */
    public long getId_currency() {
        return id_instrument_currency;
    }

    /**
     * @param id_currency the id_currency to set
     */
    public void setId_currency(long id_currency) {
        this.id_instrument_currency = id_currency;
    }

    /**
     * @param keyHashMap the keyHashMap to set
     */
    public void setKeyHashMap(HashMap<String, String> keyHashMap) {
        this.keyHashMap = keyHashMap;
    }
}

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

import java.util.Date;

public class Transaction {
    public static String TRANSACTION_TYPE_STOP="s";
    public static String TRANSACTION_TYPE_LIMITED="L";
    public static String TRANSACTION_TYPE_UNLIMITED="U";
    public static String TRANSACTION_TYPE_DIRECT="D";
    public static String TRANSACTION_TYPE_STOP_LIMITED="S";

    public static String TRANSACTION_STATUS_OPEN="O";
    public static String TRANSACTION_STATUS_CANCELED="C";
    public static String TRANSACTION_STATUS_EXECUTED="X";

    public long id_position;
    public String id_transaction;
    public String id_transaction_type;
    public Long quantity;
    public Float price;
    public Float fee;
    public Date timestamp;
    public String id_status;

    public Float underlying_stop;
    public Float underlying_entry;
    public Float underlying_target;

    public Transaction(long id_position, String id_transaction, String id_status, String id_transaction_type, Date timestamp, Float price, Float fee,
    Long quantity) {
        this.id_position=id_position;
        this.id_transaction=id_transaction;
        this.id_status=id_status;
        this.id_transaction_type=id_transaction_type;
        this.timestamp=timestamp;
        this.price=price;
        this.fee=fee;
        this.quantity=quantity;
    }

    public static String translateStatus(String statusKey) {
        // threat an submitted order as "open" as long as we do not know what happened
        // "weitergeleited", "gerouted", "offen", "entgegen genommen"
        String id=TRANSACTION_STATUS_OPEN;

        //@todo: fix the unicode issue with German umlauts, which breaks "ausgeführt"
        if (statusKey.contains("ausgef")) {
            id=TRANSACTION_STATUS_EXECUTED;
        } else if (statusKey.equalsIgnoreCase("abgelehnt") || statusKey.equalsIgnoreCase("abgelaufen") || statusKey.equalsIgnoreCase("gestrichen")) {
            id=TRANSACTION_STATUS_CANCELED;
        }
        return id;
    }

    public static String translateTransactionType(String transactionTypeKey) {
        String id=TRANSACTION_TYPE_UNLIMITED;

        //@todo: check for unlimited and direct (direct=limit?!)
        if (transactionTypeKey.equals("Limit")) {
            id=TRANSACTION_TYPE_LIMITED;
        } else if (transactionTypeKey.contains("Stop Loss") || transactionTypeKey.contains("Stop Buy")) {
            id=TRANSACTION_TYPE_STOP;
        } else if (transactionTypeKey.equals("Stop Limit")) {
            id=TRANSACTION_TYPE_STOP_LIMITED;
        }
        return id;
    }
    
    public static Long translatesQuantity(String quantityKey, Long quantity) {
        return quantityKey.equalsIgnoreCase("Verkauf") ? -1 * quantity : quantity;
    }

    public String getStatusDescription() {
        String description="";

        if (id_status.equals(TRANSACTION_STATUS_OPEN)) description="open";
        else if (id_status.equals(TRANSACTION_STATUS_EXECUTED)) description="executed";
        else if (id_status.equals(TRANSACTION_STATUS_CANCELED)) description="canceled";
         
        return description;
    }

    public String getDescription() {
        return new StringBuffer()
                .append("Order ")
                .append(id_transaction)
                .append(" ")
                .append(id_status)
                .append(" (")
                .append( quantity)
                .append("@")
                .append(price)
                .append(")")
                .toString();
    }

    public boolean isExecuted() {
        return id_status.equals(TRANSACTION_STATUS_EXECUTED);
    }

    public boolean isCanceled() {
        return id_status.equals(TRANSACTION_STATUS_CANCELED);
    }

    public boolean isOpen() {
        return id_status.equals(TRANSACTION_STATUS_OPEN);
    }

    public boolean isPurchase() {
        return quantity >= 0;
    }

    public boolean isSale() {
        return quantity < 0;
    }

    public boolean isStop() {
        return id_transaction_type.equals(TRANSACTION_TYPE_STOP);
    }

    public boolean isLimited() {
        return id_transaction_type.equals(TRANSACTION_TYPE_LIMITED);
    }

    public boolean isUnlimited() {
        return id_transaction_type.equals(TRANSACTION_TYPE_UNLIMITED);
    }


}

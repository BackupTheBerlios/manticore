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

import com.manticore.foundation.Instrument;
import com.manticore.foundation.Position;

/**
 *
 * @author are
 */
public class PositionA extends Position {

    PositionA(long id_account, long id_position, Instrument instrument, Float underlyingStop, Float underlyingEntry, float f) {
        super(id_account, id_position, instrument, underlyingStop, underlyingEntry, f);
    }
    
    public void sell(Float price, Long shares) {
        //@todo: implement some code for partial sale
        Float transactionAmount = price * shares;

        quantity += shares;
        amount += transactionAmount;

        profit += isLong() ?  (averageEntry-price) * shares : (price-averageEntry) * shares;
    }

     /**
     * @return the positionProfit
     */
    public Float getPositionProfit(Float price) {
        Float totalProfit = 0f;

        if (averageEntry != null) {
            totalProfit += isLong() ?  (averageEntry-price) * quantity : (price-averageEntry) * quantity;
        }

        if (profit != null) {
            totalProfit += profit;
        }
        return totalProfit;
    }
}

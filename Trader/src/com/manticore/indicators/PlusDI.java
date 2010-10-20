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

package com.manticore.indicators;

import com.manticore.chart.CandleArrayList;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class PlusDI extends Thread {
    CandleArrayList chartSettings;
    int k1;
    int k2;
    Core talib;
    double[] inHigh;
    double[] inLow;
    double[] inClose;
    int optInTimePeriod;

    public PlusDI(CandleArrayList chartSettings, int k1, int k2, Core talib, double[] inHigh, double[] inLow, double[] inClose, int optInTimePeriod) {
        this.chartSettings=chartSettings;
        this.k1=k1;
        this.k2=k2;
        this.talib=talib;
        this.inHigh=inHigh;
        this.inLow=inLow;
        this.inClose=inClose;
        this.optInTimePeriod=optInTimePeriod;
    }

    public void run() {
        int  allocationSize = k2 - k1 - talib.plusDILookback(optInTimePeriod) + 1;
        if (allocationSize > 0) {
            double[] outReal = new double[allocationSize];

            MInteger outBegIdx = new MInteger();
            MInteger outNBElement = new MInteger();

            RetCode returnCode = talib.plusDI(0, inHigh.length - 1, inHigh, inLow, inClose, optInTimePeriod, outBegIdx, outNBElement, outReal);

            for (int i = 0; i < outNBElement.value; i++) {
                chartSettings. get(i + k1 + outBegIdx.value).setPlusDI(outReal[i]);
            }
        }
    }
}

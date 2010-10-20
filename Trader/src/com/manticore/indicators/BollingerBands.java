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
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

public class BollingerBands extends Thread {
        CandleArrayList chartSettings;
        int k1;
        int k2;
        Core talib;
        double[] inReal;
        int optInTimePeriod;
        double optInNbDevUp;
        double optInNbDevDn;
        MAType optInMAType;

    public BollingerBands(CandleArrayList chartSettings, int k1, int k2, Core talib, double[] inReal, int optInTimePeriod, double optInNbDevUp, double optInNbDevDn, MAType optInMAType) {
        this.chartSettings=chartSettings;
        this.k1=k1;
        this.k2=k2;
        this.talib=talib;
        this.inReal=inReal;
        this.optInTimePeriod=optInTimePeriod;
        this.optInNbDevUp=optInNbDevUp;
        this.optInNbDevDn=optInNbDevDn;
        this.optInMAType=optInMAType;
    }

    //public int bbandsLookback( int optInTimePeriod, double optInNbDevUp, double optInNbDevDn, MAType optInMAType )
    // optInTimePeriod = 5; optInNbDevUp = 2.000000e+0; optInNbDevDn = 2.000000e+0;
    public void run() {
        int allocationSize = k2 - k1 - talib.bbandsLookback(optInTimePeriod, optInNbDevUp, optInNbDevDn, optInMAType) + 1;

        if (allocationSize > 0) {
            double outRealUpperBand[] = new double[allocationSize];
            double outRealMiddleBand[] = new double[allocationSize];
            double outRealLowerBand[] = new double[allocationSize];

            MInteger outBegIdx = new MInteger();
            MInteger outNBElement = new MInteger();

            RetCode returnCode = talib.bbands(0, inReal.length - 1, inReal, optInTimePeriod, optInNbDevUp, optInNbDevDn, optInMAType, outBegIdx, outNBElement, outRealUpperBand, outRealMiddleBand, outRealLowerBand);
            for (int i = 0; i < outNBElement.value; i++) {
                chartSettings.get(i + k1 + outBegIdx.value).setBB(outRealUpperBand[i], outRealMiddleBand[i], outRealLowerBand[i]);
            }
        }
    }
}

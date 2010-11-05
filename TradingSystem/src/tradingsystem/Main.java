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

package tradingsystem;

import com.manticore.chart.CandleArrayList;
import com.manticore.chart.ChartParameters;
import com.manticore.chart.PeriodSettings;
import com.manticore.database.Quotes;
import com.manticore.foundation.Candle;
import com.manticore.foundation.Instrument;
import com.manticore.foundation.StockExchange;
import com.manticore.ui.TradeSystemApplication;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;

public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // create a script engine manager
            ScriptEngineManager factory = new ScriptEngineManager();
            // create a JavaScript engine
            ScriptEngine engine = factory.getEngineByName("JavaScript");

				Iterator<ScriptEngineFactory> iterator= factory.getEngineFactories().iterator();
				while (iterator.hasNext()) {
					 System.out.println( iterator.next().getLanguageName());
				}

            // create a Java object
            String name = "Tom";
            // create the binding
            engine.put("greetingname", name);
            // evaluate JavaScript code from String
            engine.eval("println('Hello, ' + greetingname)");
            engine.eval("println('The name length is ' +  greetingname.length)");

				TradeSystemApplication tradeSystemApplication = new TradeSystemApplication();
				tradeSystemApplication.setVisible(true);

        } catch (ScriptException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}

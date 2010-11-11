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

import com.manticore.database.Quotes;

public class ImportCSVTickData {

    public static void main(String[] args) {
            Quotes.getInstance().executeSqlBatch( importCSVTickData("/tmp/trader.tickdata_1_22.csv") );
    }

    public static String importCSVTickData(String fileName) {
        StringBuffer stringBuffer = new StringBuffer()
                .append("DROP TABLE IF EXISTS trader.tickdata_tmp;\n")
                .append("CREATE TABLE trader.tickdata_tmp ")
                .append("( ")
                .append("   id_instrument smallint NOT NULL, ")
                .append("   id_stock_exchange smallint NOT NULL, ")
                .append("   \"timestamp\" timestamp NOT NULL, ")
                .append("   price double precision NOT NULL ")
                .append(");\n ")
                .append("CREATE UNIQUE INDEX \"tickdata_tmp_id_instrument_id_stock_exchange_timestamp_Idx\" ON trader.tickdata_tmp ")
                .append("( ")
                .append("   id_instrument, id_stock_exchange, \"timestamp\" ")
                .append(");\n ");

        //H2-database
//        stringBuffer.append("INSERT INTO trader.tickdata_tmp ")
//                .append("(id_instrument,id_stock_exchange,\"timestamp\",price ) SELECT * FROM CSVREAD('")
//                .append(fileName)
//                .append("');\n");

        //postgresql
        stringBuffer.append("COPY trader.tickdata_tmp ")
                .append(" FROM '")
                .append(fileName)
                .append("' WITH CSV HEADER;\n");

        stringBuffer.append("INSERT INTO trader.tickdata ").append("(id_instrument,id_stock_exchange,\"timestamp\",price ) ").append("SELECT t1.id_instrument,t1.id_stock_exchange,t1.\"timestamp\", t1.price ").append("FROM trader.tickdata_tmp t1").append("   INNER JOIN trader.stock_exchange_instrument t2 ON (t1.id_instrument=t2.id_instrument AND t1.id_stock_exchange=t2.id_stock_exchange) ").append("   LEFT JOIN trader.tickdata t3 ON (t1.id_instrument=t3.id_instrument AND t1.id_stock_exchange=t3.id_stock_exchange AND t1.\"timestamp\"=t3.\"timestamp\") ").append("WHERE t3.price IS NULL").append(";\n ");
        stringBuffer.append("DELETE FROM trader.tickdata_tmp;\n ");

        stringBuffer.append("DROP TABLE IF EXISTS trader.tickdata_tmp;\n ");
        return stringBuffer.toString();
    }
}

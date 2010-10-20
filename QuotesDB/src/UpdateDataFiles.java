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



import com.manticore.database.DataBaseWizard;
import com.manticore.database.Quotes;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateDataFiles {
    public static void main(String[] args) {
        try {
            DataBaseWizard.createDataUpdateFile("DAX30", buildTickDataPatch(22, 1));
            DataBaseWizard.createDataUpdateFile("ESTX50", buildTickDataPatch(57, 3));
            DataBaseWizard.createDataUpdateFile("EURUSD", buildTickDataPatch(27, 12));
        } catch (IOException ex) {
            Logger.getLogger(UpdateDataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String buildTickDataPatch(long id_stock_echange, long id_instrument) {
        String sqlStr=new StringBuffer()
                .append(" select ")
                .append(" * ")
                .append(" from trader.tickdata ")
                .append(" WHERE \"timestamp\">=date_trunc('day', now() - interval '7 days') ")
                .append(" and id_instrument=").append(id_instrument)
                .append(" and id_stock_exchange=").append(id_stock_echange)
                .append(" order by \"timestamp\" ")
                .append(" ; ")
                .toString();

        //@todo: DROP TABLE name IF EXISTS vs. DROP TABLE IF EXISTS name in HSQLDB
        StringBuffer stringBuffer=new StringBuffer()
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
       

        try {
            ResultSet resultSet = Quotes.getInstance().getResultSet(sqlStr);
            int i=0;
            int r=0;
            do {
                 i=0;
                 stringBuffer.append("INSERT INTO trader.tickdata_tmp ")
                              .append("(id_instrument,id_stock_exchange,\"timestamp\",price ) VALUES ");
                 while (i<5000 && resultSet.next()) {
                    if (i>0) stringBuffer.append(", ");

                    stringBuffer.append("(");
                    stringBuffer.append(resultSet.getInt("id_instrument")).append(", ");
                    stringBuffer.append(resultSet.getInt("id_stock_exchange")).append(", ");
                    stringBuffer.append("'").append(resultSet.getTimestamp("timestamp")).append("', ");
                    stringBuffer.append(resultSet.getDouble("price")).append(")");

                    i++;
                }
                stringBuffer.append(";\n ");
                stringBuffer.append("INSERT INTO trader.tickdata ")
                    .append("(id_instrument,id_stock_exchange,\"timestamp\",price ) ")
                    .append("SELECT t1.id_instrument,t1.id_stock_exchange,t1.\"timestamp\", t1.price ")
                    .append("FROM trader.tickdata_tmp t1")
                    .append("   INNER JOIN trader.stock_exchange_instrument t2 ON (t1.id_instrument=t2.id_instrument AND t1.id_stock_exchange=t2.id_stock_exchange) ")
                    .append("   LEFT JOIN trader.tickdata t3 ON (t1.id_instrument=t3.id_instrument AND t1.id_stock_exchange=t3.id_stock_exchange AND t1.\"timestamp\"=t3.\"timestamp\") ")
                    .append("WHERE t3.price IS NULL")
                    .append(";\n ");
                stringBuffer.append("DELETE FROM trader.tickdata_tmp;\n ");
                r++;
            } while (i==5000);

        } catch (SQLException ex) {
            Logger.getLogger(UpdateDataFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
        stringBuffer.append("DROP TABLE IF EXISTS trader.tickdata_tmp;\n ");
        return stringBuffer.toString();
    }

}

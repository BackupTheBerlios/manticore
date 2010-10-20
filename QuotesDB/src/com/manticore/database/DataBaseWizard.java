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
package com.manticore.database;

import com.manticore.foundation.MessageDialog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import lzma.sdk.lzma.Decoder;
import lzma.streams.LzmaInputStream;
import lzma.streams.LzmaOutputStream;

public class DataBaseWizard {

    public final static String SCHEMA_NAME = "trader";
    public final static String patchDir = "./src/com/manticore/database/patch/";
    public final static String patchResourceUrlStr = "/com/manticore/database/patch/";
    public final static String patchFileName = "manticore-database";
    public final static DataBasePatch[] patches = {
        new DataBasePatch(0, 9, 7)
        , new DataBasePatch(0, 9, 8)
        , new DataBasePatch(1, 1, 0)
        , new DataBasePatch(1, 1, 1)
        , new DataBasePatch(1, 1, 2)
    };

    public static void checkDataBase() {
        boolean found = false;
        boolean importTickData = false;

        MessageDialog.getInstance().showAndLock("Iniate the Database. Please wait.\n");

        // build initial database version 0.9.5 (no version_info exist)
        try {
            ResultSet resultSet = Quotes.getInstance().getConnection().getMetaData().getSchemas();
            while (resultSet.next() & !found) {
                found |= (resultSet.getString("TABLE_SCHEM").equalsIgnoreCase(SCHEMA_NAME));
            }

        } catch (SQLException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!found) {
            Logger.getAnonymousLogger().info("Schema not found. Generate new schema now.");
            Quotes.getInstance().executeSqlBatch(getSqlStr());

            importTickData = true;
        }

        found = false;

        // build initial database version 0.9.6 (version_info should exist, else update)
        //@todo: fix the issue of lower-case vs. upper case, HSQLDB wants upper case!
        try {
            ResultSet resultSet = Quotes.getInstance().getConnection().getMetaData().getTables(null, SCHEMA_NAME, "version_info", null);
            found = resultSet.next();

            resultSet = Quotes.getInstance().getConnection().getMetaData().getTables(null, "TRADER", "VERSION_INFO", null);
            found |= resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!found) {
            Logger.getAnonymousLogger().info("version_info not found. Update to new schema 0.9.6 now.");
            Quotes.getInstance().executeSqlBatch(getSqlStr2());

            importTickData = true;
        }

        // from version 0.9.7 we use a smart upgrade mechanism
        Logger.getAnonymousLogger().info("Update to the latest version.");
        for (int i = 0; i < patches.length; i++) {
            importTickData |= patches[i].process();
        }

        // finally import quotes
        if (importTickData) {
            MessageDialog.getInstance().showAndLock("Import quotes now. Please wait some seconds.\n");
            MessageDialog.getInstance().showAndLock("Visit the download section of http://www.manticore-projects.com for quotes of past days.\n");
            Logger.getAnonymousLogger().info("Import quotes now. Please wait some seconds.");
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        Quotes.getInstance().importTickdata();
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

        MessageDialog.getInstance().release("Ready to run the application now.\n");
    }

    private static String getSqlStr() {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE SCHEMA trader ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.ext_key ");
        sb.append("( ");
        sb.append("   id_ext_key smallint NOT NULL, description character varying(64) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.ext_key_instrument ");
        sb.append("( ");
        sb.append("   id_ext_key smallint NOT NULL, ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   value character varying(12) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.ext_key_stock_exchange_instrument ");
        sb.append("( ");
        sb.append("   id_ext_key smallint NOT NULL, ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   value character varying(12) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.import_type ");
        sb.append("( ");
        sb.append("   id_import_type smallint NOT NULL, description character varying(64) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.instrument ");
        sb.append("( ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   description character varying(64) NOT NULL, ");
        sb.append("   symbol character varying(10), ");
        sb.append("   id_instrument_type character(1) NOT NULL, ");
        sb.append("   id_instrument_currency smallint, ");
        sb.append("   id_old bigint NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.instrument_index ");
        sb.append("( ");
        sb.append("   id_instrument smallint NOT NULL, id_instrument_index smallint NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.instrument_type ");
        sb.append("( ");
        sb.append("   id_instrument_type character(1) NOT NULL, ");
        sb.append("   description character varying(64) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.tickdata ");
        sb.append("( ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   \"timestamp\" timestamp  NOT NULL, ");
        sb.append("   price double precision NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.stock_exchange ");
        sb.append("( ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   description character varying(64) NOT NULL, ");
        sb.append("   symbol character varying(12) NOT NULL, ");
        sb.append("   opening_minute smallint NOT NULL, ");
        sb.append("   closing_minute smallint NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.stock_exchange_excluded_interval ");
        sb.append("( ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   interval_start timestamp  NOT NULL, ");
        sb.append("   interval_end timestamp  NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.stock_exchange_instrument ");
        sb.append("( ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   id_import_type smallint NOT NULL, ");
        sb.append("   has_quantity boolean DEFAULT false NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.time_marker ");
        sb.append("( ");
        sb.append("   \"timestamp\" timestamp  NOT NULL, ");
        sb.append("   short_description character varying(64) NOT NULL, ");
        sb.append("   long_description varchar(255) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.transaction ");
        sb.append("( ");
        sb.append("   id_transaction bigint NOT NULL, ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   quantity integer DEFAULT 0 NOT NULL, ");
        sb.append("   price numeric(10,5) NOT NULL, ");
        sb.append("   fee numeric(10,5) NOT NULL, ");
        sb.append("   amount numeric(10,5) NOT NULL, ");
        sb.append("   up_date timestamp  DEFAULT now() NOT NULL, ");
        sb.append("   id_instrument_type character(1) NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE TABLE trader.volumedata ");
        sb.append("( ");
        sb.append("   id_instrument smallint NOT NULL, ");
        sb.append("   id_stock_exchange smallint NOT NULL, ");
        sb.append("   \"timestamp\" timestamp  NOT NULL, ");
        sb.append("   quantity bigint NOT NULL ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.ext_key ADD CONSTRAINT pkext_key PRIMARY KEY (id_ext_key) ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.import_type ADD CONSTRAINT pkimport_type PRIMARY KEY ");
        sb.append("( ");
        sb.append("   id_import_type ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.instrument ADD CONSTRAINT pkinstrument PRIMARY KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.instrument_type ADD CONSTRAINT pkinstrument_type PRIMARY KEY ");
        sb.append("( ");
        sb.append("   id_instrument_type ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.stock_exchange ADD CONSTRAINT pkstock_exchange PRIMARY KEY ");
        sb.append("( ");
        sb.append("   id_stock_exchange ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.transaction ADD CONSTRAINT pktransaction PRIMARY KEY ");
        sb.append("( ");
        sb.append("   id_transaction ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX \"ext_key_instrument_id_ext_key_id_instrument_Idx\" ON trader.ext_key_instrument ");
        sb.append("( ");
        sb.append("   id_ext_key, id_instrument ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX ext_key_stock_exchange_instrument_id_ext_key_id_stock_exchange_ ON trader.ext_key_stock_exchange_instrument ");
        sb.append("( ");
        sb.append("   id_ext_key, id_stock_exchange, id_instrument ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX \"instrument_index_id_instrument_id_instrument_index_Idx\" ON trader.instrument_index ");
        sb.append("( ");
        sb.append("   id_instrument, id_instrument_index ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE INDEX \"stock_exchange_excluded_interval_id_stock_exchange_Idx\" ON trader.stock_exchange_excluded_interval ");
        sb.append("( ");
        sb.append("   id_stock_exchange ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX \"stock_exchange_instrument_id_stock_exchange_id_instrument_Idx\" ON trader.stock_exchange_instrument ");
        sb.append("( ");
        sb.append("   id_stock_exchange, id_instrument ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX \"tickdata_id_instrument_id_stock_exchange_timestamp_Idx\" ON trader.tickdata ");
        sb.append("( ");
        sb.append("   id_instrument, id_stock_exchange, \"timestamp\" ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE INDEX \"time_marker_timestamp_Idx\" ON trader.time_marker (\"timestamp\") ");
        sb.append("; ");
        sb.append("CREATE INDEX \"transaction_id_instrument_up_date_id_transaction_type_Idx\" ON trader.transaction ");
        sb.append("( ");
        sb.append("   id_instrument, up_date, id_instrument_type ");
        sb.append(") ");
        sb.append("; ");
        sb.append("CREATE UNIQUE INDEX \"volumedata_id_instrument_id_stock_exchange_timestamp_Idx\" ON trader.volumedata ");
        sb.append("( ");
        sb.append("   id_instrument, id_stock_exchange, \"timestamp\" ");
        sb.append(") ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.ext_key_instrument ADD CONSTRAINT fk_ext_key_instrument_ext_key FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_ext_key ");
        sb.append(") ");
        sb.append("REFERENCES trader.ext_key(id_ext_key) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.ext_key_instrument ADD CONSTRAINT fk_ext_key_instrument_instrument FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.ext_key_stock_exchange_instrument ADD CONSTRAINT fk_ext_key_stock_exchange_instrument_ext_key FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_ext_key ");
        sb.append(") ");
        sb.append("REFERENCES trader.ext_key(id_ext_key) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.instrument_index ADD CONSTRAINT fk_instrument_index_instrument FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.instrument_index ADD CONSTRAINT fk_instrument_index_instrument2 FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument_index ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE RESTRICT ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.instrument ADD CONSTRAINT fk_instrument_instrument_type FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument_type ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument_type(id_instrument_type) ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.stock_exchange_instrument ADD CONSTRAINT fk_stock_exchange_instrument_import_type FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_import_type ");
        sb.append(") ");
        sb.append("REFERENCES trader.import_type(id_import_type) ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.stock_exchange_instrument ADD CONSTRAINT fk_stock_exchange_instrument_instrument FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.tickdata ADD CONSTRAINT fk_tickdata_stock_exchange FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_stock_exchange ");
        sb.append(") ");
        sb.append("REFERENCES trader.stock_exchange(id_stock_exchange) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.volumedata ADD CONSTRAINT fk_volumedata_stock_exchange FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_stock_exchange ");
        sb.append(") ");
        sb.append("REFERENCES trader.stock_exchange(id_stock_exchange) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.volumedata ADD CONSTRAINT fk_volumedata_instrument FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ON ");
        sb.append("UPDATE CASCADE ON ");
        sb.append("DELETE CASCADE ");
        sb.append("; ");
        sb.append("ALTER TABLE trader.transaction ADD CONSTRAINT fk_transaction_instrument FOREIGN KEY ");
        sb.append("( ");
        sb.append("   id_instrument ");
        sb.append(") ");
        sb.append("REFERENCES trader.instrument(id_instrument) ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (1, 'Ariva Stream') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (2, 'Consors') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (3, 'Deutsche Bank X-Markets') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (4, 'Boerse Go') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (5, 'ISIN') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key ");
        sb.append("VALUES (6, 'WKN') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.instrument_type ");
        sb.append("VALUES ('I', 'Index') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.instrument_type ");
        sb.append("VALUES ('S', 'share') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.instrument_type ");
        sb.append("VALUES ('C', 'commodity') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.instrument_type ");
        sb.append("VALUES ('P', 'pair') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.instrument ");
        sb.append("VALUES (1, 'DAX 30', 'DAX', 'I', NULL, 133962) ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key_instrument ");
        sb.append("VALUES (6, 1, '846900') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key_instrument ");
        sb.append("VALUES (5, 1, 'DE0008469008') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key_instrument ");
        sb.append("VALUES (3, 1, '18') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key_instrument ");
        sb.append("VALUES (4, 1, '133962') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.stock_exchange ");
        sb.append("VALUES (22, 'Lang und Schwarz', 'l&s', 480, 1320) ");
        sb.append("; ");
        sb.append("INSERT INTO trader.ext_key_stock_exchange_instrument ");
        sb.append("VALUES (1, 22, 1, '867764@16') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.import_type ");
        sb.append("VALUES (1, 'ticks from boerse-go.de') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.import_type ");
        sb.append("VALUES (2, '1 minute candles from consors.de') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.import_type ");
        sb.append("VALUES (3, 'import by ISIN') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.import_type ");
        sb.append("VALUES (4, 'import commodities') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.import_type ");
        sb.append("VALUES (5, 'ticks from godmodetrader devisen-tracker') ");
        sb.append("; ");
        sb.append("INSERT INTO trader.stock_exchange_instrument ");
        sb.append("VALUES (22, 1, 1, false) ");
        sb.append("; ");

        return sb.toString();
    }

    private static String getSqlStr2() {
        StringBuffer sb = new StringBuffer();
        sb.append("ALTER TABLE trader.transaction DROP CONSTRAINT fk_transaction_instrument; ");
        sb.append("CREATE TABLE trader.account ");
        sb.append("( ");
        sb.append("	id_account SMALLINT NOT NULL, ");
        sb.append("	id_broker SMALLINT NOT NULL ");
        sb.append("); ");
        sb.append("ALTER TABLE trader.account ADD CONSTRAINT pkaccount ");
        sb.append("	PRIMARY KEY (id_account); ");
        sb.append("CREATE INDEX \"account_id_broker_Idx\" ON trader.account (id_broker); ");
        sb.append("CREATE SEQUENCE trader.broker_id_broker_seq; ");
        sb.append("CREATE TABLE trader.broker ");
        sb.append("( ");
        sb.append("	id_broker SMALLINT NOT NULL, ");
        sb.append("	description VARCHAR(128) NOT NULL, ");
        sb.append("	symbol VARCHAR(12) NOT NULL ");
        sb.append("); ");
        sb.append("ALTER TABLE trader.broker ADD CONSTRAINT pkbroker ");
        sb.append("	PRIMARY KEY (id_broker); ");
        sb.append("ALTER TABLE trader.instrument_index ADD ratio FLOAT; ");
        sb.append("CREATE TABLE trader.position ");
        sb.append("( ");
        sb.append("	id_position SMALLINT NOT NULL, ");
        sb.append("	id_position_type CHAR(1) NOT NULL, ");
        sb.append("	id_position_status CHAR(1) NOT NULL, ");
        sb.append("	id_instrument SMALLINT NOT NULL, ");
        sb.append("	isin VARCHAR(12) NOT NULL, ");
        sb.append("	average_entry DOUBLE PRECISION NOT NULL, ");
        sb.append("	position_amount NUMERIC(10, 2) NOT NULL, ");
        sb.append("	profit NUMERIC(10, 2) NOT NULL, ");
        sb.append("	id_account SMALLINT NOT NULL ");
        sb.append("); ");
        sb.append("ALTER TABLE trader.position ADD CONSTRAINT pkposition ");
        sb.append("	PRIMARY KEY (id_position); ");
        sb.append("CREATE INDEX \"position_id_position_status_Idx\" ON trader.position (id_position_status); ");
        sb.append("CREATE TABLE trader.tan ");
        sb.append("( ");
        sb.append("	id_tan_card SMALLINT, ");
        sb.append("	\"key\" VARCHAR(12), ");
        sb.append("	value VARCHAR(64), ");
        sb.append("	valid boolean ");
        sb.append("); ");
        sb.append("CREATE UNIQUE INDEX \"tan_id_tan_card_key_Idx\" ON trader.tan (id_tan_card, \"key\"); ");
        sb.append("CREATE INDEX \"tan_valid_Idx\" ON trader.tan (valid); ");
        sb.append("CREATE TABLE trader.tan_card ");
        sb.append("( ");
        sb.append("	id_tan_card SMALLINT NOT NULL, ");
        sb.append("	id_account SMALLINT ");
        sb.append("); ");
        sb.append("ALTER TABLE trader.tan_card ADD CONSTRAINT tan_card_pkey ");
        sb.append("	PRIMARY KEY (id_tan_card); ");
        sb.append("CREATE UNIQUE INDEX \"tan_card_id_tan_card_id_account_Idx\" ON trader.tan_card (id_tan_card, id_account); ");
        sb.append("ALTER TABLE trader.time_marker RENAME TO time_marker_old; ");
        sb.append("CREATE TABLE trader.time_marker ");
        sb.append("( ");
        sb.append("	\"timestamp\" TIMESTAMP NOT NULL, ");
        sb.append("	short_description VARCHAR(64) NOT NULL, ");
        sb.append("	long_description VARCHAR(255) NOT NULL ");
        sb.append("); ");
        sb.append("INSERT INTO trader.time_marker ");
        sb.append("	 (\"timestamp\", short_description, long_description) ");
        sb.append("SELECT \"timestamp\", short_description, long_description ");
        sb.append("FROM trader.time_marker_old; ");
        sb.append("DROP TABLE trader.time_marker_old CASCADE; ");
        sb.append("CREATE INDEX \"time_marker_timestamp_Idx\" ON trader.time_marker (\"timestamp\"); ");
        sb.append("ALTER TABLE trader.transaction RENAME TO transaction_old; ");
        sb.append("CREATE TABLE trader.transaction ");
        sb.append("( ");
        sb.append("	id_position SMALLINT NOT NULL, ");
        sb.append("	id_transaction BIGINT NOT NULL, ");
        sb.append("	id_transaction_type CHAR(1) NOT NULL, ");
        sb.append("	quantity INTEGER NOT NULL, ");
        sb.append("	price NUMERIC(10, 5) NOT NULL, ");
        sb.append("	fee NUMERIC(10, 2) NOT NULL, ");
        sb.append("	amount NUMERIC(10, 2) NOT NULL, ");
        sb.append("	\"timestamp\" TIMESTAMP NOT NULL, ");
        sb.append("	id_status CHAR(1) NOT NULL, ");
        sb.append("	underlying_stop DOUBLE PRECISION NOT NULL, ");
        sb.append("	underlying_entry DOUBLE PRECISION NOT NULL, ");
        sb.append("	underlying_target DOUBLE PRECISION NOT NULL ");
        sb.append("); ");
        sb.append("INSERT INTO trader.transaction ");
        sb.append("	 (id_transaction, quantity, price, fee, amount) ");
        sb.append("SELECT id_transaction, quantity, price, fee, amount ");
        sb.append("FROM trader.transaction_old; ");
        sb.append("DROP TABLE trader.transaction_old CASCADE; ");
        sb.append("CREATE UNIQUE INDEX \"transaction_id_position_id_transaction_Idx\" ON trader.transaction (id_position, id_transaction); ");
        sb.append("CREATE INDEX \"transaction_id_status_Idx\" ON trader.transaction (id_status); ");
        sb.append("CREATE INDEX \"transaction_id_transaction_Idx\" ON trader.transaction (id_transaction); ");
        sb.append("CREATE TABLE trader.version_info ");
        sb.append("( ");
        sb.append("	major_version SMALLINT NOT NULL, ");
        sb.append("	minor_version SMALLINT NOT NULL, ");
        sb.append("	patch_level SMALLINT NOT NULL, ");
        sb.append("	installation_start TIMESTAMP, ");
        sb.append("	installation_end TIMESTAMP ");
        sb.append("); ");
        sb.append("CREATE UNIQUE INDEX \"version_info_major_version_minor_version_patch_level_Idx\" ON trader.version_info (major_version, minor_version, patch_level); ");
        sb.append("INSERT INTO trader.version_info VALUES (0,9,6,now(),null); ");
        sb.append("ALTER TABLE trader.account ADD CONSTRAINT fk_account_broker ");
        sb.append("	FOREIGN KEY (id_broker) REFERENCES trader.broker (id_broker) ON UPDATE CASCADE ON DELETE RESTRICT; ");
        sb.append("ALTER TABLE trader.ext_key_stock_exchange_instrument ADD CONSTRAINT fk_ext_key_stock_exchange_instrument_instrument ");
        sb.append("	FOREIGN KEY (id_instrument) REFERENCES trader.instrument (id_instrument) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.ext_key_stock_exchange_instrument ADD CONSTRAINT fk_ext_key_stock_exchange_instrument_stock_exchange ");
        sb.append("	FOREIGN KEY (id_stock_exchange) REFERENCES trader.stock_exchange (id_stock_exchange) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.position ADD CONSTRAINT fk_position_account ");
        sb.append("	FOREIGN KEY (id_account) REFERENCES trader.account (id_account) ON UPDATE NO ACTION ON DELETE NO ACTION; ");
        sb.append("ALTER TABLE trader.position ADD CONSTRAINT fk_position_instrument ");
        sb.append("	FOREIGN KEY (id_instrument) REFERENCES trader.instrument (id_instrument) ON UPDATE CASCADE ON DELETE RESTRICT; ");
        sb.append("ALTER TABLE trader.stock_exchange_excluded_interval ADD CONSTRAINT fk_stock_exchange_excluded_interval_stock_exchange ");
        sb.append("	FOREIGN KEY (id_stock_exchange) REFERENCES trader.stock_exchange (id_stock_exchange) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.stock_exchange_instrument ADD CONSTRAINT fk_stock_exchange_instrument_stock_exchange ");
        sb.append("	FOREIGN KEY (id_stock_exchange) REFERENCES trader.stock_exchange (id_stock_exchange) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.tan ADD CONSTRAINT tan_id_tan_card_fkey ");
        sb.append("	FOREIGN KEY (id_tan_card) REFERENCES trader.tan_card (id_tan_card) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.tan_card ADD CONSTRAINT fk_tan_card_account ");
        sb.append("	FOREIGN KEY (id_account) REFERENCES trader.account (id_account) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.tickdata ADD CONSTRAINT fk_tickdata_instrument ");
        sb.append("	FOREIGN KEY (id_instrument) REFERENCES trader.instrument (id_instrument) ON UPDATE CASCADE ON DELETE CASCADE; ");
        sb.append("ALTER TABLE trader.transaction ADD CONSTRAINT fk_transaction_position ");
        sb.append("	FOREIGN KEY (id_position) REFERENCES trader.position (id_position) ON UPDATE NO ACTION ON DELETE NO ACTION; ");
        sb.append("INSERT INTO trader.broker VALUES (1, 'flatex AG', 'flatex'); ");
        sb.append("INSERT INTO trader.account VALUES (1, 1); ");
        sb.append("UPDATE trader.version_info SET installation_end=now() WHERE major_version=0 AND minor_version=9 and patch_level=6; ");
        return sb.toString();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int nread = 0;
        final byte[] buffer = new byte[1024];
        while ((nread = in.read(buffer)) != -1) {
            out.write(buffer, 0, nread);
            out.flush();
        }
    }

    public static void applyUpdateFromFile(final String filename) {
        MessageDialog.getInstance().showAndLock("apply updates from " + filename + "\n");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                Logger.getAnonymousLogger().log(Level.INFO, "apply updates from {0}", filename);
                String updateScript = getTextFromCompressedFile(filename);

                Quotes.getInstance().executeSqlBatch(updateScript);
                MessageDialog.getInstance().release("done.\n");
            }
        });

    }

    public static String getTextFromCompressedFile(String filename) {
        String s = "";
        LzmaInputStream compressedIn = null;
        try {
            File compressed = new File(filename);
            compressedIn = new LzmaInputStream(new BufferedInputStream(new FileInputStream(compressed)), new Decoder());

             try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            copy(compressedIn, byteArrayOutputStream);
            s = new String(byteArrayOutputStream.toByteArray(), Charset.forName("UTF-8"));
            compressedIn.close();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

        } catch (IOException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    public static String getTextFromCompressedInputStream(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        LzmaInputStream compressedIn = null;
        try {
            compressedIn = new LzmaInputStream(new BufferedInputStream(inputStream), new Decoder());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(compressedIn));
                      
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            compressedIn.close();
            reader.close();

        } catch (IOException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

    public static void createDatabasePatchFile(int major_version, int minor_version, int patch_level) throws IOException {
        File sourceFile = new File(buildTempPatchFileName(major_version, minor_version, patch_level));
        File compressed = new File(buildPatchFileName(major_version, minor_version, patch_level));

        final LzmaOutputStream compressedOut = new LzmaOutputStream.Builder(
                new BufferedOutputStream(new FileOutputStream(compressed))).useMaximalDictionarySize().useEndMarkerMode(true).useBT4MatchFinder().build();

        final InputStream sourceIn = new BufferedInputStream(new FileInputStream(sourceFile));

        copy(sourceIn, compressedOut);
        sourceIn.close();
        compressedOut.close();
    }

    public static void createDataUpdateFile(String label, String source) throws IOException {
        //File compressed = File.createTempFile("manticore-trader-" + label, ".dbu");
        File compressed = new File ("/tmp/manticore-trader-" + label+".dbu");
        
        final LzmaOutputStream compressedOut = new LzmaOutputStream.Builder(
                new BufferedOutputStream(new FileOutputStream(compressed))).useMaximalDictionarySize().useEndMarkerMode(true).useBT4MatchFinder().build();
        final InputStream sourceIn = new BufferedInputStream(new ByteArrayInputStream(source.getBytes(Charset.forName("UTF-8"))));

        copy(sourceIn, compressedOut);
        sourceIn.close();
        compressedOut.close();
    }

    public static void test_round_trip() throws IOException {
        final File sourceFile = new File("/home/are/manticore-database-0.9.7.sql");
        final File compressed = File.createTempFile("manticore-trader-0.9.7", ".dbu");
        final File unCompressed = File.createTempFile("manticore-trader-0.9.7", ".uncompressed");

        final LzmaOutputStream compressedOut = new LzmaOutputStream.Builder(
                new BufferedOutputStream(new FileOutputStream(compressed))).useMaximalDictionarySize().useEndMarkerMode(true).useBT4MatchFinder().build();

        final InputStream sourceIn = new BufferedInputStream(new FileInputStream(sourceFile));

        copy(sourceIn, compressedOut);
        sourceIn.close();
        compressedOut.close();

        final LzmaInputStream compressedIn = new LzmaInputStream(
                new BufferedInputStream(new FileInputStream(compressed)),
                new Decoder());

        final OutputStream uncompressedOut = new BufferedOutputStream(
                new FileOutputStream(unCompressed));

        copy(compressedIn, uncompressedOut);
        compressedIn.close();
        uncompressedOut.close();
    }



    public static String buildPatchFileName(int major_version, int minor_version, int patch_level) {
        return new StringBuffer().append(patchDir).append(patchFileName).append("-").append(major_version).append(".").append(minor_version).append(".").append(patch_level).append(".dbu").toString();
    }

    public static String buildPatchFileUrlStr(int major_version, int minor_version, int patch_level) {
        return new StringBuffer().append(patchResourceUrlStr).append(patchFileName).append("-").append(major_version).append(".").append(minor_version).append(".").append(patch_level).append(".dbu").toString();
    }

    public static String buildTempPatchFileName(int major_version, int minor_version, int patch_level) {
        String tempDirStr = System.getProperty("java.io.tmpdir");
        if (!tempDirStr.endsWith(File.separator)) {
            tempDirStr += File.separator;
        }

        return new StringBuffer().append(tempDirStr).append(patchFileName).append("-").append(major_version).append(".").append(minor_version).append(".").append(patch_level).append(".sql").toString();
    }

    public static void main(String[] args) {
        try {
            createDatabasePatchFile(1, 1, 2);
//            System.out.println(getTextFromCompressedFile("/tmp/manticore-trader-ESTX50.dbu"));
            System.out.println(getTextFromCompressedFile(buildPatchFileName(1, 1, 2)));
        } catch (IOException ex) {
            Logger.getLogger(DataBaseWizard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

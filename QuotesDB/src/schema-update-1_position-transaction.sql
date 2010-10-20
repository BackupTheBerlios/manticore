/************ Remove Foreign Keys ***************/
ALTER TABLE trader.transaction DROP CONSTRAINT fk_transaction_instrument;


/************ Update: Tables ***************/

/******************** Add Table: trader.position ************************/
 CREATE SEQUENCE trader.position_id_position_seq INCREMENT 1;

/* Build Table Structure */
CREATE TABLE trader.position
(
	id_position SMALLINT NOT NULL DEFAULT nextval('trader.position_id_position_seq'),
	id_position_type CHAR(1) NOT NULL,
	id_position_status CHAR(1) NOT NULL,
	id_instrument SMALLINT NOT NULL,
	isin VARCHAR(12) NOT NULL,
	average_price FLOAT NOT NULL,
	amount DECIMAL(10, 2) NOT NULL,
	profit DECIMAL(10, 2) NOT NULL,
	id_instrument_type CHAR(1) NULL
);

/* Table Items: trader.position */
ALTER TABLE trader.position ADD CONSTRAINT pkposition
	PRIMARY KEY (id_position);

/* Add Indexes for: position */
CREATE INDEX "position_id_position_status_Idx" ON trader.position (id_position_status);


/************ Rebuild Table: trader.transaction ***************/

/* Rename: trader.transaction */
ALTER TABLE trader.transaction RENAME TO transaction_old;

/* Build Table Structure */
CREATE TABLE trader.transaction
(
	id_position SMALLINT NOT NULL,
	id_transaction BIGINT NOT NULL,
	id_transaction_type CHAR(1) NOT NULL,
	quantity INTEGER NOT NULL,
	price DECIMAL(10, 5) NOT NULL,
	fee DECIMAL(10, 2) NOT NULL,
	amount DECIMAL(10, 2) NOT NULL,
	"timestamp" TIMESTAMP NOT NULL,
	id_status CHAR(1) NOT NULL,
	underlying_stop FLOAT NOT NULL,
	underlying_entry FLOAT NOT NULL,
	underlying_target FLOAT NOT NULL
);

/* Repopulate Table Data */
INSERT INTO trader.transaction
	 (id_transaction, quantity, price, fee, amount)
SELECT id_transaction, quantity, price, fee, amount
FROM trader.transaction_old;

/* Remove Temp Table */
DROP TABLE trader.transaction_old CASCADE;

/* Table Items: trader.transaction */

/* Add Indexes for: transaction */
CREATE INDEX "transaction_id_status_Idx" ON trader.transaction (id_status);
CREATE INDEX "transaction_id_transaction_Idx" ON trader.transaction (id_transaction);

/******************** Add Table: trader.transaction_old ************************/

/* Build Table Structure */
CREATE TABLE trader.transaction_old
(
	id_transaction BIGINT NOT NULL,
	id_instrument SMALLINT NOT NULL,
	quantity INTEGER NOT NULL DEFAULT 0,
	price NUMERIC(10, 5) NOT NULL,
	fee NUMERIC(10, 5) NOT NULL,
	amount NUMERIC(10, 5) NOT NULL,
	up_date TIMESTAMP NOT NULL DEFAULT now(),
	id_instrument_type CHAR(1) NOT NULL
) WITHOUT OIDS;

/* Table Items: trader.transaction_old */
ALTER TABLE trader.transaction_old ADD CONSTRAINT pktransaction_old
	PRIMARY KEY (id_transaction);

/* Add Indexes for: transaction_old */
CREATE INDEX "transaction_id_instrument_up_date_id_transaction_type_Idx" ON trader.transaction_old (id_instrument, up_date, id_instrument_type);


/************ Add Foreign Keys to Database ***************/

/************ Foreign Key: fk_position_instrument ***************/
ALTER TABLE trader.position ADD CONSTRAINT fk_position_instrument
	FOREIGN KEY (id_instrument) REFERENCES trader.instrument (id_instrument) ON UPDATE CASCADE ON DELETE RESTRICT;

/************ Foreign Key: fk_transaction_position ***************/
ALTER TABLE trader.transaction ADD CONSTRAINT fk_transaction_position
	FOREIGN KEY (id_position) REFERENCES trader.position (id_position) ON UPDATE NO ACTION ON DELETE NO ACTION;

/************ Foreign Key: fk_transaction_instrument ***************/
ALTER TABLE trader.transaction_old ADD CONSTRAINT fk_transaction_instrument
	FOREIGN KEY (id_instrument) REFERENCES trader.instrument (id_instrument) ON UPDATE NO ACTION ON DELETE NO ACTION;

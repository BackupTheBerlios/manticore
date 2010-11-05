copy trader.instrument to '/tmp/trader.instrument.csv' with CSV HEADER;

copy trader.instrument_index to '/tmp/trader.instrument_index.csv' with CSV HEADER;

copy trader.stock_exchange to '/tmp/trader.stock_exchange.csv' with CSV HEADER;

copy trader.stock_exchange_instrument to '/tmp/trader.stock_exchange_instrument.csv' with CSV HEADER;

copy trader.ext_key_instrument to '/tmp/trader.ext_key_instrument.csv' with CSV HEADER;

copy trader.ext_key_stock_exchange_instrument to '/tmp/trader.ext_key_stock_exchange_instrument.csv' with CSV HEADER;

copy trader.stock_exchange_excluded_interval to '/tmp/trader.stock_exchange_excluded_interval.csv' with CSV HEADER;

copy (select * from trader.tickdata where id_instrument=1 and id_stock_exchange=22) to '/tmp/trader.tickdata_1_22.csv' with CSV HEADER;

copy (select * from trader.tickdata where id_instrument=3 and id_stock_exchange=57) to '/tmp/trader.tickdata_3_57.csv' with CSV HEADER;
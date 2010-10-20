#!/bin/bash
PACKAGES="Broker ChartExport Foundation InvestmentReport PositionController QuotesDB SwingUI Trader Utilities WebsiteParser lib"

tar --exclude-vcs --exclude **/dist --exclude **/build -cjf manticore.tar.bz2 $PACKAGES



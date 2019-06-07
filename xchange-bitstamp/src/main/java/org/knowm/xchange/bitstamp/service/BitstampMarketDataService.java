package org.knowm.xchange.bitstamp.service;

import java.io.IOException;
import java.util.function.Consumer;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bitstamp.BitstampAdapters;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampTicker;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.service.marketdata.MarketDataService;

/** @author Matija Mazi */
public class BitstampMarketDataService extends BitstampMarketDataServiceRaw
    implements MarketDataService {

  public BitstampMarketDataService(Exchange exchange) {
    super(exchange);
  }

  @Override
  public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
    BitstampTicker bitstampTicker = getBitstampTicker(currencyPair);

    return BitstampAdapters.adaptTicker(bitstampTicker, currencyPair);
  }

  @Override
  public void onTickerEvent(CurrencyPair currencyPair, Consumer<Ticker> consumer, Object... args) throws IOException {
    onBitstampTicker(currencyPair, consumer);
  }

  @Override
  public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
    return BitstampAdapters.adaptOrderBook(getBitstampOrderBook(currencyPair), currencyPair);
  }

  @Override
  public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
    BitstampTime time = args.length > 0 ? (BitstampTime) args[0] : null;
    return BitstampAdapters.adaptTrades(getTransactions(currencyPair, time), currencyPair);
  }
}

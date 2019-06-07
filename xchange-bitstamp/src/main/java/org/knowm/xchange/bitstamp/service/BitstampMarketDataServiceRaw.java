package org.knowm.xchange.bitstamp.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampV2;
import org.knowm.xchange.bitstamp.dto.BitstampException;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampOrderBook;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampTicker;
import org.knowm.xchange.bitstamp.dto.marketdata.BitstampTransaction;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.mazi.rescu.RestProxyFactory;

/**
 * @author gnandiga
 */
public class BitstampMarketDataServiceRaw extends BitstampBaseService {

  private final BitstampV2 bitstampV2;
  private final WebSocketClient bitstampWebSocket;
  protected final Logger logger = LoggerFactory.getLogger(getClass());


  public BitstampMarketDataServiceRaw(Exchange exchange) {

    super(exchange);
    ExchangeSpecification exchangeSpecification = exchange.getExchangeSpecification();
    this.bitstampV2 =
        RestProxyFactory
            .createProxy(BitstampV2.class, exchangeSpecification.getSslUri(), getClientConfig());

    URI webSocketUri = null;
    try {
      webSocketUri = new URI(exchangeSpecification.getWsUri());
    } catch (URISyntaxException e) {
      logger.error(e.getMessage(), e);
    }
    this.bitstampWebSocket = new WebSocketClient(webSocketUri) {
      @Override
      public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Open connection to {}", uri);
      }

      @Override
      public void onMessage(String s) {
        logger.info(s);
      }

      @Override
      public void onClose(int i, String s, boolean b) {
        logger.info("Close connection to {}", uri);
      }

      @Override
      public void onError(Exception e) {
        logger.error("Error with message to {}", uri);
      }
    };

    try {
      bitstampWebSocket.connectBlocking();
    } catch (InterruptedException e) {
      logger.error("Error wile connection to {}", webSocketUri);
    }

  }

  public BitstampTicker getBitstampTicker(CurrencyPair pair) throws IOException {
    try {
      return bitstampV2.getTicker(new BitstampV2.Pair(pair));
    } catch (BitstampException e) {
      throw handleError(e);
    }
  }

  public void onBitstampTicker(CurrencyPair pair, Consumer<Ticker> consumer) throws IOException {
    try {
      bitstampWebSocket.send("{\"event\":\"bts:subscribe\",\"data\":{\"channel\":\"live_trades_btcusd\"}}");
    } catch (BitstampException e) {
      throw handleError(e);
    }
  }

  public BitstampOrderBook getBitstampOrderBook(CurrencyPair pair) throws IOException {

    try {
      return bitstampV2.getOrderBook(new BitstampV2.Pair(pair));
    } catch (BitstampException e) {
      throw handleError(e);
    }
  }

  public BitstampTransaction[] getTransactions(CurrencyPair pair, @Nullable BitstampTime time)
      throws IOException {

    try {
      return bitstampV2.getTransactions(new BitstampV2.Pair(pair), time);
    } catch (BitstampException e) {
      throw handleError(e);
    }
  }

  public enum BitstampTime {
    DAY,
    HOUR,
    MINUTE;

    @Override
    public String toString() {
      return super.toString().toLowerCase();
    }
  }
}

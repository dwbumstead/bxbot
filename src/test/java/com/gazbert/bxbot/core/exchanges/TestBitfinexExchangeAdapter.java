package com.gazbert.bxbot.core.exchanges;

import com.gazbert.bxbot.core.api.trading.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.junit.Assert.*;

/**
 * <p>
 * Tests the behaviour of the Bitfinex Exchange Adapter.
 * </p>
 *
 * <p>
 * Coverage could be better: it does not include calling the {@link BitfinexExchangeAdapter#sendPublicRequestToExchange(String)}
 * and {@link BitfinexExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} methods; the code in these methods
 * is a bloody nightmare to test!
 * </p>
 *
 * TODO Unit test {@link BitfinexExchangeAdapter#sendPublicRequestToExchange(String)} method.
 * TODO Unit test {@link BitfinexExchangeAdapter#sendAuthenticatedRequestToExchange(String, Map)} method.
 *
 * @author gazbert
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.crypto.*")
@PrepareForTest(BitfinexExchangeAdapter.class)
public class TestBitfinexExchangeAdapter {

    // Valid config location - expected on runtime classpath in the ./src/test/resources folder.
    private static final String VALID_CONFIG_LOCATION = "bitfinex/bitfinex-config.properties";

    // Canned JSON responses from exchange - expected to reside on filesystem relative to project root
    private static final String BOOK_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/book.json";
    private static final String ORDERS_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/orders.json";
    private static final String BALANCE_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/balances.json";
    private static final String PUB_TICKER_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/pubticker.json";
    private static final String ACCOUNT_INFOS_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/account_infos.json";
    private static final String ORDER_NEW_BUY_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/order_new_buy.json";
    private static final String ORDER_NEW_SELL_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/order_new_sell.json";
    private static final String ORDER_CANCEL_JSON_RESPONSE = "./src/test/exchange-data/bitfinex/order_cancel.json";

    // Exchange API calls
    private static final String BOOK = "book";
    private static final String ORDERS = "orders";
    private static final String BALANCES = "balances";
    private static final String PUB_TICKER = "pubticker";
    private static final String ACCOUNT_INFOS = "account_infos";
    private static final String ORDER_NEW = "order/new";
    private static final String ORDER_CANCEL = "order/cancel";

    // Canned test data
    private static final String MARKET_ID = "btcusd";
    private static final BigDecimal BUY_ORDER_PRICE = new BigDecimal("200.18");
    private static final BigDecimal BUY_ORDER_QUANTITY = new BigDecimal("0.03");
    private static final BigDecimal SELL_ORDER_PRICE = new BigDecimal("250.176");
    private static final BigDecimal SELL_ORDER_QUANTITY = new BigDecimal("0.03");
    private static final String ORDER_ID_TO_CANCEL = "426152651";

    // Mocked out methods
    private static final String MOCKED_GET_CONFIG_LOCATION_METHOD = "getConfigFileLocation";
    private static final String MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD = "sendAuthenticatedRequestToExchange";
    private static final String MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD = "sendPublicRequestToExchange";


    // ------------------------------------------------------------------------------------------------
    //  Create Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCreateOrderToBuyIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_NEW_BUY_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_NEW), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);
        assertTrue(orderId.equals("425116925"));

        PowerMock.verifyAll();
    }

    @Test
    public void testCreateOrderToSellIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_NEW_SELL_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_NEW), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final String orderId = exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
        assertTrue(orderId.equals("425116929"));

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCreateOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_NEW), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Marion, don't look at it. Shut your eyes, Marion. Don't look at" +
                        " it, no matter what happens!"));

        PowerMock.replayAll();

        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCreateOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_NEW), anyObject(Map.class)).
                andThrow(new IllegalArgumentException("What a fitting end to your life's pursuits. You're about to " +
                        "become a permanent addition to this archaeological find. Who knows? In a thousand years," +
                        " even you may be worth something."));

        PowerMock.replayAll();

        exchangeAdapter.createOrder(MARKET_ID, OrderType.BUY, BUY_ORDER_QUANTITY, BUY_ORDER_PRICE);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Cancel Order tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testCancelOrderIsSuccessful() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDER_CANCEL_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_CANCEL), anyObject(Map.class)).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final boolean success = exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);
        assertTrue(success);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testCancelOrderHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_CANCEL), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Good morning. I am Meredith Vickers, and it is my job to make sure you do yours"));

        PowerMock.replayAll();

        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testCancelOrderHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDER_CANCEL), anyObject(Map.class)).
                andThrow(new IllegalStateException("he ring, it chose you. Take it... place the ring on the lantern..." +
                        " place the ring, speak the oath... great honor... responsibility"));

        PowerMock.replayAll();

        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Market Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingMarketOrdersSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BOOK_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, BOOK + "/" + MARKET_ID).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final MarketOrderBook marketOrderBook = exchangeAdapter.getMarketOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(marketOrderBook.getMarketId().equals(MARKET_ID));

        final BigDecimal buyPrice = new BigDecimal("239.43");
        final BigDecimal buyQuantity = new BigDecimal("5.0");
        final BigDecimal buyTotal = buyPrice.multiply(buyQuantity);

        assertTrue(marketOrderBook.getBuyOrders().size() == 906); // 'finex sends them all back!
        assertTrue(marketOrderBook.getBuyOrders().get(0).getType() == OrderType.BUY);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getPrice().compareTo(buyPrice) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getQuantity().compareTo(buyQuantity) == 0);
        assertTrue(marketOrderBook.getBuyOrders().get(0).getTotal().compareTo(buyTotal) == 0);

        final BigDecimal sellPrice = new BigDecimal("239.53");
        final BigDecimal sellQuantity = new BigDecimal("6.35595596");
        final BigDecimal sellTotal = sellPrice.multiply(sellQuantity);

        assertTrue(marketOrderBook.getSellOrders().size() == 984); // 'finex sends them all back!
        assertTrue(marketOrderBook.getSellOrders().get(0).getType() == OrderType.SELL);
        assertTrue(marketOrderBook.getSellOrders().get(0).getPrice().compareTo(sellPrice) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getQuantity().compareTo(sellQuantity) == 0);
        assertTrue(marketOrderBook.getSellOrders().get(0).getTotal().compareTo(sellTotal) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingMarketOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, BOOK + "/" + MARKET_ID).
                andThrow(new ExchangeTimeoutException("There are three basic types, Mr. Pizer: the Wills, the Won'ts," +
                        " and the Can'ts. The Wills accomplish everything, the Won'ts oppose everything, and the " +
                        "Can'ts won't try anything."));

        PowerMock.replayAll();

        exchangeAdapter.getMarketOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingMarketOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, BOOK + "/" + MARKET_ID).
                andThrow(new IllegalArgumentException("Deckard. B26354"));

        PowerMock.replayAll();

        exchangeAdapter.getMarketOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Your Open Orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingYourOpenOrdersSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ORDERS_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDERS),
                     anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final List<OpenOrder> openOrders = exchangeAdapter.getYourOpenOrders(MARKET_ID);

        // assert some key stuff; we're not testing GSON here.
        assertTrue(openOrders.size() == 2);
        assertTrue(openOrders.get(0).getMarketId().equals(MARKET_ID));
        assertTrue(openOrders.get(0).getId().equals("423760243"));
        assertTrue(openOrders.get(0).getType() == OrderType.SELL);
        assertTrue(openOrders.get(0).getCreationDate().getTime() == 1442073766);
        assertTrue(openOrders.get(0).getPrice().compareTo(new BigDecimal("259.38")) == 0);
        assertTrue(openOrders.get(0).getQuantity().compareTo(new BigDecimal("0.03")) == 0);
        assertTrue(openOrders.get(0).getOriginalQuantity().compareTo(new BigDecimal("0.03")) == 0);
        assertTrue(openOrders.get(0).getTotal().compareTo(openOrders.get(0).getPrice().multiply(openOrders.get(0).getOriginalQuantity())) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingYourOpenOrdersHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDERS), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("There's an entirely different universe beyond that black hole. " +
                        "A point where time and space as we know it no longer exists. We will be the first to see it, " +
                        "to explore it, to experience it!"));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingYourOpenOrdersHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ORDERS), anyObject(Map.class)).
                andThrow(new IllegalStateException("Nope, I can't make it! My main circuits are gone, my " +
                        "anti-grav-systems blown, and both backup systems are failing"));

        PowerMock.replayAll();

        exchangeAdapter.getYourOpenOrders(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Latest Market Price tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingLatestMarketPriceSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(PUB_TICKER_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, PUB_TICKER + "/" + MARKET_ID).
                andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal latestMarketPrice = exchangeAdapter.getLatestMarketPrice(MARKET_ID).setScale(8, BigDecimal.ROUND_HALF_UP);
        assertTrue(latestMarketPrice.compareTo(new BigDecimal("236.07")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingLatestMarketPriceHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, PUB_TICKER + "/" + MARKET_ID).
                andThrow(new ExchangeTimeoutException("They say most of your brain shuts down in cryo-sleep. " +
                        "All but the primitive side, the animal side. No wonder I'm still awake. Transporting me with " +
                        "civilians. Sounded like 40, 40-plus. Heard an Arab voice. Some hoodoo holy man, probably on " +
                        "his way to New Mecca. But what route? What route? I smelt a woman. Sweat, boots, tool belt," +
                        " leather. Prospector type. Free settlers. And they only take the back roads. And here's my " +
                        "real problem. Mr. Johns... the blue-eyed devil. Planning on taking me back to slam... " +
                        "only this time he picked a ghost lane. A long time between stops. A long time for something" +
                        " to go wrong..."));

        PowerMock.replayAll();

        exchangeAdapter.getLatestMarketPrice(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingLatestMarketPriceHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_PUBLIC_REQUEST_TO_EXCHANGE_METHOD, PUB_TICKER + "/" + MARKET_ID).
                andThrow(new IllegalArgumentException(" All you people are so scared of me. Most days I'd take that as" +
                        " a compliment. But it ain't me you gotta worry about now"));

        PowerMock.replayAll();

        exchangeAdapter.getLatestMarketPrice(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Balance Info tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingBalanceInfoSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(BALANCE_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCES),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BalanceInfo balanceInfo = exchangeAdapter.getBalanceInfo();

        // assert some key stuff; we're not testing GSON here.
        assertTrue(balanceInfo.getBalancesAvailable().get("BTC").compareTo(new BigDecimal("0.1267283")) == 0);
        assertTrue(balanceInfo.getBalancesAvailable().get("USD").compareTo(new BigDecimal("0")) == 0);

        // Bitfinex does not provide "balances on hold" info.
        assertNull(balanceInfo.getBalancesOnHold().get("BTC"));
        assertNull(balanceInfo.getBalancesOnHold().get("LTC"));

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingBalanceInfoHandlesExchangeTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCES), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException(" Don't know, I don't know such stuff. I just do eyes, ju-, ju-," +
                        " just eyes... just genetic design, just eyes. You Nexus, huh? I design your eyes"));

        PowerMock.replayAll();

        exchangeAdapter.getBalanceInfo();

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingBalanceInfoHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(BALANCES), anyObject(Map.class)).
                andThrow(new IllegalStateException(" I've seen things you people wouldn't believe. Attack ships on fire" +
                        " off the shoulder of Orion. I watched C-beams glitter in the dark near the Tannhauser gate. " +
                        "All those moments will be lost in time... like tears in rain... Time to die"));

        PowerMock.replayAll();

        exchangeAdapter.getBalanceInfo();

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Exchange Fees for Buy orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingExchangeBuyingFeeSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ACCOUNT_INFOS_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal buyPercentageFee = exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(buyPercentageFee.compareTo(new BigDecimal("0.0020")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingExchangeBuyingFeeHandlesTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Right. Well, um, using layman's terms... Use a retaining magnetic" +
                        " field to focus a narrow beam of gravitons - these, in turn, fold space-time consistent with" +
                        " Weyl tensor dynamics until the space-time curvature becomes infinitely large, and you produce" +
                        " a singularity. Now, the singularity..."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingExchangeBuyingFeeHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS), anyObject(Map.class)).
                andThrow(new IllegalStateException("I created the Event Horizon to reach the stars, but she's gone much," +
                        " much farther than that. She tore a hole in our universe, a gateway to another dimension." +
                        " A dimension of pure chaos. Pure... evil. When she crossed over, she was just a ship." +
                        " But when she came back... she was alive! Look at her, Miller. Isn't she beautiful?"));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Get Exchange Fees for Sell orders tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingExchangeSellingFeeSuccessfully() throws Exception {

        // Load the canned response from the exchange
        final byte[] encoded = Files.readAllBytes(Paths.get(ACCOUNT_INFOS_JSON_RESPONSE));
        final String exchangeResponse = new String(encoded, StandardCharsets.UTF_8);

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMockAndInvokeDefaultConstructor(
                BitfinexExchangeAdapter.class, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS),
                anyObject(Map.class)).andReturn(exchangeResponse);

        PowerMock.replayAll();

        final BigDecimal buyPercentageFee = exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);
        assertTrue(buyPercentageFee.compareTo(new BigDecimal("0.0020")) == 0);

        PowerMock.verifyAll();
    }

    @Test (expected = ExchangeTimeoutException.class )
    public void testGettingExchangeSellingFeeHandlesTimeoutException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS), anyObject(Map.class)).
                andThrow(new ExchangeTimeoutException("Day 11, Test 37, Configuration 2.0. For lack of a better option," +
                        " Dummy is still on fire safety."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    @Test (expected = TradingApiException.class)
    public void testGettingExchangeSellingFeeHandlesUnexpectedException() throws Exception {

        // Partial mock so we do not send stuff down the wire
        final BitfinexExchangeAdapter exchangeAdapter =  PowerMock.createPartialMock(BitfinexExchangeAdapter.class,
                MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD);
        PowerMock.expectPrivate(exchangeAdapter, MOCKED_SEND_AUTHENTICATED_REQUEST_TO_EXCHANGE_METHOD, eq(ACCOUNT_INFOS), anyObject(Map.class)).
                andThrow(new IllegalStateException("What was made public about the Event Horizon - that she was a deep" +
                        " space research vessel, that her reactor went critical, and that the ship blew up - none of " +
                        "that is true. The Event Horizon is the culmination of a secret government project to create a" +
                        " spacecraft capable of faster-than-light flight."));

        PowerMock.replayAll();

        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Non Exchange visiting tests
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testGettingImplNameIsAsExpected() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final BitfinexExchangeAdapter exchangeAdapter = new BitfinexExchangeAdapter();
        assertTrue(exchangeAdapter.getImplName().equals("Bitfinex API v1"));

        PowerMock.verifyAll();
    }

    // ------------------------------------------------------------------------------------------------
    //  Initialisation tests - assume config property files are located under src/test/resources
    // ------------------------------------------------------------------------------------------------

    @Test
    public void testExchangeAdapterInitialisesSuccessfully() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

        final BitfinexExchangeAdapter exchangeAdapter = new BitfinexExchangeAdapter();
        assertNotNull(exchangeAdapter);

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfPublicKeyConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitfinex/missing-public-key-bitfinex-config.properties");
        PowerMock.replayAll();

        new BitfinexExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfSecretConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitfinex/missing-secret-bitfinex-config.properties");
        PowerMock.replayAll();

        new BitfinexExchangeAdapter();

        PowerMock.verifyAll();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testExchangeAdapterThrowsExceptionIfTimeoutConfigIsMissing() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(
                "bitfinex/missing-timeout-bitfinex-config.properties");
        PowerMock.replayAll();

        new BitfinexExchangeAdapter();

        PowerMock.verifyAll();
    }

    /*
     * Used for making real API calls to the exchange in order to grab JSON responses.
     * Have left this in; it might come in useful.
     * It expects VALID_CONFIG_LOCATION to contain the correct credentials.
     */
//    @Test
    public void testCallingExchangeToGetJson() throws Exception {

        // Partial mock the adapter so we can manipulate config location
        PowerMock.mockStaticPartial(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD);
        PowerMock.expectPrivate(BitfinexExchangeAdapter.class, MOCKED_GET_CONFIG_LOCATION_METHOD).andReturn(VALID_CONFIG_LOCATION);
        PowerMock.replayAll();

//        final TradingApi exchangeAdapter = new BitfinexExchangeAdapter();
//        exchangeAdapter.getImplName();
//        exchangeAdapter.getPercentageOfBuyOrderTakenForExchangeFee(MARKET_ID);
//        exchangeAdapter.getPercentageOfSellOrderTakenForExchangeFee(MARKET_ID);
//        exchangeAdapter.getLatestMarketPrice(MARKET_ID);
//        exchangeAdapter.getYourOpenOrders(MARKET_ID);
//        exchangeAdapter.getBalanceInfo();

//        exchangeAdapter.createOrder(MARKET_ID, OrderType.SELL, SELL_ORDER_QUANTITY, SELL_ORDER_PRICE);
//        exchangeAdapter.cancelOrder(ORDER_ID_TO_CANCEL);

        PowerMock.verifyAll();
    }
}
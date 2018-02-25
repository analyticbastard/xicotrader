# xicotrader

[![CircleCI](https://circleci.com/gh/analyticbastard/xicotrader.svg?style=shield)](https://circleci.com/gh/analyticbastard/xicotrader)
[![Deps](https://versions.deps.co/analyticbastard/xicotrader/status.svg)](https://versions.deps.co/analyticbastard/xicotrader)
[![codecov](https://codecov.io/gh/analyticbastard/xicotrader/branch/master/graph/badge.svg)](https://codecov.io/gh/analyticbastard/xicotrader)

Automatically trade cryptocurrency: a growing ex-academic example.

## Current state

The software currently consists of a automatic trading client, and a simulator that mocks
a data and order service.

The system loads the strategy from an external JAR file using [pomegranate](https://github.com/cemerick/pomegranate), which
must implement a Clojure protocol (Java interface) defined in this project. This
protocol is the entry point to the strategy, which is fed with data as this trading
client passes it on, along with the current portfolio state.

### Example of a trading strategy

A simple arbitrage strategy is implemented in the subproject folder `arbitrage`
to illustrate the automatic trading.
Upon each tick and current state, the strategy selects the best assets to cycle
from USD, BTC and ETH.

You need to build this project (see instructions within) and place the resulting
JAR file anywhere in the classpath **TBD**, so that the project can load up the classes within the JAR.

## Usage

### Grab some data

The simulator understands the Poloniex API format and assumes a 5 min inter-tick period, e.g.,

```
https://poloniex.com/public?command=returnChartData&currencyPair=BTC_ETH&end=9999999999&period=300&start=1405699200
```

Under resources, create a directory called ```marketdata``` and place your
three market data files called ```btcusd.json```, ```ethbtc.json``` and 
```ethusd.json```.

Download the data from all three pairs and store them in files with the aforementioned names, e.g., 

```
curl https://poloniex.com/public?command=returnChartData&currencyPair=BTC_ETH&end=9999999999&period=300&start=1405699200 > ethbtc.json
curl https://poloniex.com/public?command=returnChartData&currencyPair=USDT_BTC&end=9999999999&period=300&start=1405699200 > btcusd.json
curl https://poloniex.com/public?command=returnChartData&currencyPair=USDT_ETH&end=9999999999&period=300&start=1405699200 > ethusd.json
```

This is an example intended to use with a couple of REPLs.

```bash
lein with-profile simulator,dev repl
```

On another terminal

```bash
lein with-profile client,dev repl
```

Then issue ```(start)``` on each of the REPLs, starting with the simulator.

The simulator runs so that every 10 minutes is scaled down to 1 second, so you
can see things very quickly.

The simulator gets the first data not already in the past according to the simulator
time. So things are expected not to exactly align with a real situation.


### Use in real trading

The software is simple so the basic components should be robust. However the
software comes as is and the author offers no warranty.

For a real situation, a service that implements connectivity with your exchange 
can be implemented and added to the system dependencies in sustitution of the
component that implements connectivity with the simulator.

### Building

```bash
lein with-profile +client uberjar
```

### Testing

```bash
lein with-profile dev,client test
```

### Code coverage

```bash
CLOVERAGE_VERSION=1.0.7-SNAPSHOT lein with-profile dev,client cloverage --codecov
```

## Architecture

The software uses Stuart Sierra's [component](https://github.com/stuartsierra/component) library
and each component feeds data into the system and receives actions from the inner components that
are processed and passed on outwards. This component communication is done with
[core async](https://github.com/clojure/core.async).

The system is very simple and consists of only a small number of components

```
+------------+           +------------+c-from s-to+------------+
|            |           |            <-----------+            |
|            |  compute  |            |           |            |
|  Strategy  +-----------+  Strategy  |           | Controller |
|  impl      |           |  holder    |           |            |
|            |           |            |     s-from|            |
|            |           |            +----------->            |
+------------+           +------------+c-to       +--+------^--+
                                                 send|      |rec
                                                     |      |
                                                     |      |
                                                     |      |
                                     +------------+  |      |  +------------+
                                     |            |  |      |  |            |
                                     |  Service   <--+      +--+  Service   |
                                     |  sender    |con      con|  receiver  |
                                     |            |            |            |
                                     |            |            |            |
                                     |            |            |            |
                                     +-----+------+            +------+-----+
                                           |send                      |
                                           |                          |
                                           |      +------------+      |
                                           |      |            |      |
                                           |      |            |      |
                                           +------+  connector +------+
                                                  |  impl      |receive
                                                  |            |
                                                  |            |
                                                  +------------+
```

When the system is initialized from the configuration files, it loads up the strategy implementation
and the service connector implementations from external JARs using [pomegranate](https://github.com/cemerick/pomegranate).

The service connector implementation is expected to connect to an exchange (with parameters potentially
passed as configuration from files) and start receiving data. It will then call a method from the
recevier (whose reference has been passed on at init time in the form of an interface called
and injected by a dependency). The receiver will then
asynchronously send the data to the Controller, which oversees that everything is well (alarms and monitoring
will go here) and passes it on to the strategy holder, which has a reference to the externally
loaded strategy implementation. It will call it with the data feed (which includes tick data 
and potentially the most up to date portfolio according to what the exchange has sent). The strategy will then
compute a number of actions, one per coin trading pair, and sent it all the way back.
The service sender will synchronously call the service connector with the actions to be processed.

Notice that the asynchronicity is dealt with in the internal components (strategy holder,
sender and receiver), thus sparing the implementations
from dealing with asynchronicity or core.async details.

The names of the endpoints in the diagram are the names of the core.async channels and the
interface methods.

### TODO

Immediate roadmap:

- I/O schema for the ticker and trades service and for the strategy
- Loading system for independent ticker and trades service like the one for strategies and 
separate the code for the simulator service into an independent project generating its own JAR
- Carry over these changes to the arbitrage example strategy
- Improve code coverage
- Increase number of log messages
- Build strategy and data feed service JARs and use them in tests 

Future roadmap:

- Binance ticker and trades service (as a separate JAR)
- Dockerization
- Clojurescript interface

## License

Copyright © 2017 Javier Arriero

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

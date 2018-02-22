# xicotrader

[![CircleCI](https://circleci.com/gh/analyticbastard/xicotrader.svg?style=shield)](https://circleci.com/gh/analyticbastard/xicotrader)
[![Deps](https://versions.deps.co/analyticbastard/xicotrader/status.svg)](https://versions.deps.co/analyticbastard/xicotrader)
[![codecov](https://codecov.io/gh/analyticbastard/xicotrader/branch/master/graph/badge.svg)](https://codecov.io/gh/analyticbastard/xicotrader)

Automatically trade cryoptocurrency: a growing ex-academic example.

## Current state

The software currently consists of a automatic trading client, and a simulator that mocks
a data and order service.

The system loads the strategy from an external JAR file using [pomegranate](), which
must implement a Clojure protocol (Java interface) defined in this project. This
protocol is the entry point to the strategy, which is fed with data as this trading
client passes it on, along with the current portfolio state.

### Example of a trading strategy

A simple arbitrage strategy is implemented in the subproject folder `arbitrage`
to illustrate the automatic trading.
Upon each tick and current state, the strategy selects the best assets to cycle
from USD, BTC and ETH.

You need to build this project (see instructions within) and place the resulting
JAR file in **TBD**, so that the project can load up the classes within the JAR.

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

### Testing

```bash
lein with-profile +dev,+client test
```

### Code coverage

```bash
CLOVERAGE_VERSION=1.0.7-SNAPSHOT lein with-profile +dev,+client cloverage --codecov
```


## License

Copyright © 2017 Javier Arriero

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

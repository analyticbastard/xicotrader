# Xicotrader Arbitrage

Simple arbitrage strategy for Xicotrader

## Build

Build this project with

```bash
lein uberjar
```

## Load the JAR into Xicotrader

Place the JAR file built in the previous step somewhere in Xicotrader's classpath.
Specify the JAR file as a dependence in the Strategy module:

```clojure
{:strategy "arbitrage.jar"}
```

Run the Xicotrader system

## License

Copyright © 2018 Javier Arriero

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

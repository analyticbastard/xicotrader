(ns xicotrader.util)

(defmacro ignoring-exceptions [& body]
  `(try ~@body
        (catch Exception e#)))
(ns xicotrader.portfolio)

(defn update-portfolio [portfolio portfolio-updates]
  (merge portfolio
         portfolio-updates))

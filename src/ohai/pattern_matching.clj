(ns ohai.pattern-matching
  (:require [clojure.core.logic :as l]))

;; (ƒ i-like-zeroes
;;   0 0 -> "double zeroes!"
;;   0 a -> "zero car"
;;   a 0 -> "zero cdr"
;;   a b -> "no zeroes"

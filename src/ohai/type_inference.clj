(ns ohai.type-inference
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :as l :refer [run fresh ==]]))

;; (ƒ succ
;;   a → (+ a 1))

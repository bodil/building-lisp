;;; Clojure port of http://dysphoria.net/code/hindley-milner/HindleyMilner.scala

(ns hindley-milner
  (:require [clojure.string :as str]))

(declare occurs-in? occurs-in-type?)

(defn map-state
  "Evaluate a list of state monad values in sequence, producing
   a list of the results of each evaluation."
  [state mvs]
  (reduce (fn [[values state] next]
            (let [[value state] (next state)]
              [(conj values value) state]))
          [[] state]
          mvs))

(defprotocol Stringy
  (to-string [this state]))

(defprotocol SyntaxNode
  (analyse [this]))

(defprotocol IState
  (fresh-var [this])
  (fresh-lvar [this t])
  (generic? [this t])
  (lookup-env [this name])
  (bind-env [this name value])
  (set-nongen [this t])
  (bind-lvar [this t lvar])
  (unscope [this other])
  (prune [this t])
  (unify [this t1 t2])
  (-unified [this])
  (-env [this])
  (-nongen [this])
  (-lvars [this])
  (-next [this]))

(defrecord Ident [name]
  Stringy
  (to-string [_ state] name))

(defrecord Lambda [arg body]
  Stringy
  (to-string [_ state]
    (str "(fn " arg " ⇒ " (to-string body state) ")")))

(defrecord Apply [func arg]
  Stringy
  (to-string [_ state]
    (str "(" (to-string func state) " " (to-string arg state) ")")))

(defrecord Let [v def body]
  Stringy
  (to-string [_ state]
    (str "(let " v " = " (to-string def state) " in " (to-string body state) ")")))

(defrecord Letrec [v def body]
  Stringy
  (to-string [_ state]
    (str "(letrec " v " = " (to-string def state) " in " (to-string body state) ")")))

(defrecord Variable [id]
  Stringy
  (to-string [this state]
    (let [pruned (prune state this)]
      (if (= pruned this)
        id
        (to-string pruned state)))))

(defrecord Oper [name args]
  Stringy
  (to-string [_ state]
    (cond
     (zero? (count args)) name
     (= 2 (count args)) (str "(" (to-string (first args) state) " " name
                             " " (to-string (second args) state) ")")
     :else (str name " " (str/join " " (map #(to-string % state) args))))))

(defn function [from to]
  (Oper. "→" [from to]))

(def integer (Oper. "int" []))
(def bool (Oper. "bool" []))

(deftype State [unified env nongen lvars next]
  IState
  (fresh-var [_]
    (let [inced (-> next int inc char)]
      [(Variable. (str next)) (State. unified env nongen lvars inced)]))

  (fresh-lvar [this t]
    (if-let [lvar (lvars t)]
      [lvar this]
      (let [[lvar state] (fresh-var this)]
        [lvar (bind-lvar state t lvar)])))

  (bind-lvar [this t lvar]
    (State. unified env nongen (assoc lvars t lvar) next))

  (generic? [this t]
    (not (occurs-in? this nongen t)))

  (lookup-env [_ name]
    (env name))

  (bind-env [_ name value]
    (State. unified (assoc env name value) nongen lvars next))

  (set-nongen [_ t]
    (State. unified env (conj nongen t) lvars next))

  (unscope [_ other]
    (State. (-unified other) env nongen lvars (-next other)))

  (prune [this t]
    (if (and (instance? Variable t)
             (contains? unified t))
      (prune this (unified t))
      t))

  (unify [this t1 t2]
    (let [t1 (prune this t1)
          t2 (prune this t2)]
      (cond

       (and (instance? Variable t1) (not= t1 t2))
       (if (occurs-in-type? this t1 t2)
         (throw (Error. (str "recursive unification between " t1 " and " t2)))
         (State. (assoc unified t1 t2) env nongen lvars next))

       (and (instance? Oper t1) (instance? Variable t2))
       (unify this t2 t1)

       (and (instance? Oper t1) (instance? Oper t2))
       (if (or (not= (:name t1) (:name t2))
               (not= (count (:args t1)) (count (:args t2))))
         (throw (Error. (str "Type mismatch: " (to-string t1 this)
                             " ≠ " (to-string t2 this))))
         (loop [state this a (:args t1) b (:args t2)]
           (if (and (seq a) (seq b))
             (recur (unify state (first a) (first b)) (rest a) (rest b))
             state)))

       :else (throw (Error. (str "Cannot unify args " t1 " and " t2))))))

  (-unified [_] unified)
  (-env [_] env)
  (-nongen [_] nongen)
  (-lvars [_] lvars)
  (-next [_] next)

  Object
  (toString [_]
    (let [env (dissoc env "pair" "true" "cond" "zero" "pred" "times")]
      (str "<State: " unified " :: " env " :: " nongen
           " :: " lvars " :: " next ">"))))

(defn fresh-state []
  (let [state (State. {} {} #{} {} \α)
        [var1 state] (fresh-var state)
        [var2 state] (fresh-var state)
        [var3 state] (fresh-var state)
        pairtype (Oper. "×" [var1 var2])]
    (-> state
        (bind-env "pair" (function var1 (function var2 pairtype)))
        (bind-env "true" bool)
        (bind-env "cond" (function bool (function var3 (function var3 var3))))
        (bind-env "zero" (function integer bool))
        (bind-env "pred" (function integer integer))
        (bind-env "times" (function integer (function integer integer))))))

(defn occurs-in? [^State state ^Variable t l]
  (some #(occurs-in-type? state t %) l))

(defn occurs-in-type? [^State state ^Variable v type2]
  (let [v (prune state v)]
    (cond
     (= v type2) true
     (instance? Oper type2) (occurs-in? state v (:args type2))
     :else false)))

(defn integer-literal? [name]
  (re-matches #"^(\d+)$" name))

(defn fresh [t]
  (fn [^State state]
    (let [t (prune state t)]
      (cond
       (instance? Variable t)
       (if-not (generic? state t)
         [t state]
         (fresh-lvar state t))

       (instance? Oper t)
       (let [{:keys [name args]} t]
         (let [[args state]
               (map-state state (map fresh args))]
           [(Oper. name args) state]))

       :else
       (throw (Error. (str "Don't know how to fresh " t)))))))

(defn get-type [name]
  (fn [^State state]
    (let [sym (lookup-env state name)]
      (cond
       sym
       ((fresh sym) state)

       (integer-literal? name)
       [integer state]

       :else
       (throw (Error. (str "Undefined symbol " name " ::: " state)))))))

(extend-protocol SyntaxNode
  Ident
  (analyse [{:keys [name]}]
    (fn [^State state]
      ((get-type name) state)))

  Lambda
  (analyse [{:keys [arg body]}]
    (fn [^State state]
      (let [[argtype state] (fresh-var state)
            new-state (-> state
                          (bind-env arg argtype)
                          (set-nongen argtype))
            [resulttype new-state] ((analyse body) new-state)]
        [(function argtype resulttype) (unscope state new-state)])))

  Let
  (analyse [{:keys [v def body]}]
    (fn [^State state]
      (let [[deftype new-state] ((analyse def) state)
            new-state (bind-env new-state v deftype)
            [bodytype new-state] ((analyse body) new-state)]
        [bodytype (unscope state new-state)])))

  Apply
  (analyse [{:keys [func arg]}]
    (fn [^State state]
      (let [[functype state] ((analyse func) state)
            [argtype state] ((analyse arg) state)
            [resulttype state] (fresh-var state)
            state (unify state (function argtype resulttype) functype)]
        [resulttype state])))

  Letrec
  (analyse [{:keys [v def body]}]
    (fn [^State state]
      (let [[newtype new-state] (fresh-var state)
            new-state (-> new-state
                          (bind-env v newtype)
                          (set-nongen newtype))
            [deftype new-state] ((analyse def) new-state)
            new-state (unify new-state newtype deftype)
            [bodytype new-state] ((analyse body) new-state)]
        [bodytype (unscope state new-state)]))))





(comment ;; Factorial example
  (let [state (fresh-state)

        program
        (Letrec. "factorial"
                 (Lambda. "n"
                          (Apply.
                           (Apply.
                            (Apply. (Ident. "cond")
                                    (Apply. (Ident. "zero") (Ident. "n")))
                            (Ident. "1"))
                           (Apply.
                            (Apply. (Ident. "times") (Ident. "n"))
                            (Apply. (Ident. "factorial")
                                    (Apply. (Ident. "pred") (Ident. "n"))))))
                 (Apply. (Ident. "factorial") (Ident. "5")))

        _ (println (to-string program state))
        result ((analyse program) state)]
    (println (apply to-string result))
    (apply to-string result))
  )

(comment ;; Example with type errors
  (let [state (fresh-state)

        program
        (Lambda. "x"
                 (Apply.
                  (Apply. (Ident. "pair")
                          (Apply. (Ident. "x") (Ident. "3")))
                  (Apply. (Ident. "x") (Ident. "true"))))

        _ (println (to-string program state))
        result ((analyse program) state)]
    (println (apply to-string result))
    (apply to-string result))
  )

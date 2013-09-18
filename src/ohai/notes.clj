;;; Talk notes

;; Clojure: lispy, elegant, powerful, hammock time
;; Haskell: pure, typed, warm fuzzy things

;;; Instaparse example

(def p
  (insta/parser "

<EXPR> = SEXP | NUMBER
<SEXP> = <'('> ADD | NEG <')'>
ADD = <'add'> SPACE NUMBER SPACE NUMBER
NEG = <'neg'> SPACE NUMBER
<NUMBER> = #'\\d+'
<SPACE> = <#'\\s+'>

"))

;;; Primitive types

(lnumber 1337)
(lnumber? (lnumber 1337))
(lcons (lnumber 13) (lnumber 37))
(lcons (lnumber 13) (lcons (lnumber 37) nil))

;;; Eval + state monads

(defn def-m [sym val]
  (fn [env]
    [nil (assoc env sym val)]))

(def-m :foo 1300)

(defn add-m [a b]
  (fn [env]
    [(+ (get env a) (get env b)) env]))

(defn program []
  (monad/reduce-state
   {}
   [(def-m :foo 1300)
    (def-m :bar 37)
    (add-m :foo :bar)]))

;; Show eval/primtypes state monad functions.

;;; Lexical scoping

;; You must attach scope when the lambda is created,
;; not when it's invoked.
;; Show lambda type.
;; Show eval/lambda.

;;; Pattern matching

(l/run 1 [q a b]
  (l/== q [a b])
  (l/== q [0 1]))

;;; Type inference

(l/run 1 [succ]
  (l/fresh [a b +]
    (l/== succ [:fun a b])
    (l/== + [:fun a b])
    (l/== + [:fun :number :number])))

;;; Hindley-Milner

;; Run factorial. Returns int.

;; Run pair thing. Throws type error.
;; Change bool to number. Type checks.

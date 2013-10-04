;;; BODOL code

(map (λ n → (+ n 1)) '(1 2 3 4 5))

;;; BODOL code

(map (λ n → (+ n 1)) '(1 2 3 4 5))

;;; Instaparse example

(def p
  (insta/parser "

<SEXP> = <'('> NUMBER (SPACE NUMBER)* <')'>
NUMBER = #'\\d+'
<SPACE> = <#'\\s+'>

"))

(def p
  (insta/parser "

<SEXP> = NUMBER | SYMBOL | <'('> SEXP (SPACE SEXP)* <')'>
SYMBOL = #'\\w+'
NUMBER = #'\\d+'
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

((def-m :foo 1300) {})

(defn add-m [a b]
  (fn [env]
    [(+ (get env a) (get env b)) env]))

((add-m :a :b) {:a 1300 :b 37})

[(def-m :foo 1300)
    (def-m :bar 37)
    (add-m :foo :bar)]

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

(run 1 [a b]
  (fresh [q]
    (== q [0 0])   ; signature to match
    (== q [0 1]))) ; invoked arguments

;;; Type inference

(run 1 [succ]
  (fresh [a b +]
    (== succ [:fun a b])
    (== + [:fun a :number b])
    (== + [:fun :number :number :number])))

;;; Type checker

(bodol.parser/parse "(ƒ double n → (+ n n))")
(bodol.repl/eval "(ƒ double n → (+ n n))")
(-> (parser/parse "(ƒ double n → (+ n n))") type-check result-str)

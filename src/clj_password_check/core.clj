(ns clj-password-check.core
  (:require [clojure.contrib.string :as string])
  )

(def ^{:doc "return last checker function name"} last-checker (ref nil))

(defn- not-nil? [b] (-> b nil? not))
(defn- in-range? [n min max] (and (>= n min) (< n max)))
(defn- re-contains? [re s] (not-nil? (re-find re s)))
(defn- set-last-checker [f] (dosync (ref-set last-checker (-> f meta :name))))

(defn combine-checkers-or
  "return function which combining checker functions with OR operator"
  [& fns] (fn [s] (not-nil? (some #(do (set-last-checker %) (% s)) fns))))
(defn combine-checkers-and
  "return function whick combining checker functions with AND operator"
  [& fns] (fn [s] (every? #(do (set-last-checker %) (% s)) fns)))
(def ^{:doc "same as checker-combine-and" :arglists '([& fns])}
  combine-checkers combine-checkers-and)

; blank checker
(defn not-blank?
  "return boolean whether passowrd is blank or not"
  [s] (-> s string/blank? not))

; alphabet checker
(defn contains-uppercase?
  "return boolean whether password contain uppercase alphabet characters or not"
  [s] (re-contains? #"[A-Z]" s)
  )
(defn contains-lowercase?
  "return boolean whether password contain lowercase alphabet characters or not"
  [s] (re-contains? #"[a-z]" s)
  )
(defn contains-alphabet?
  "return boolean whether password contain alphabet character or not"
  [s] ((combine-checkers-or contains-lowercase? contains-uppercase?) s)
  )

; number checker
(defn contains-number?
  "return boolean whether password contain number characters or not"
  [s] (re-contains? #"[0-9]" s)
  )

; symbol checker
(defn contains-symbol?
  "return boolean whether password contain symbols or not"
  [s]
  (not-nil?
    (some #(let [in? (partial in-range? (int %))]
             (or (in? 33 48) (in? 58 65) (in? 91 96) (in? 123 127)))
          s))
  )

; character checker
(defn not-same-characters?
  "return boolean whether password characters are same or not
  ex) (not-same-characters? \"aaaaa\")
      ; false"
  [s]
  (if-let [c (first s)]
    (not-nil? (some #(not= % c) s))
    false
    )
  )
(defn not-sequential-password?
  "return boolean whether password is not sequencial or not
  ex) (not-sequential-password? \"abcdefg\")
      ; false (this is sequencial password)"
  [s]
  (let [l (map #(apply - %) (partition 2 1 (map int s)))]
    (if (empty? l) true
      (not (or (every? #(= -1 %) l) (every? #(= 1 %) l)))
      )
    )
  )

; multi byte character checker
; cf. http://www.alqmst.co.jp/tech/040601.html
(defn not-contains-multi-byte-character?
  "return boolean whether password do not contain multi byte characters or not"
  [s]
  (every? #(let [i (int %)]
             (or (<= i 126) (= i 165) (= i 8254) ;\u007e, \u00a5, \u203e
                      (in-range? i 65377 65440) ;\uff61 - \uff9f
                      )) s)
  )

; length checker
(defn length-range
  "return function which return boolean whether password length is in specified range or not
  ex) (length-range 3 5)
      ; 3 <= length <= 5
      (length-range 3)
      ; 3 <= length"
  ([min-len max-len]
   (with-meta
     (fn [s] (let [l (count s)] (and (if (nil? min-len) true (>= l min-len))
                                     (if (nil? max-len) true (<= l max-len)))))
     {:name 'length-range}
     )
   )
  ([min-len] (length-range min-len nil))
  )

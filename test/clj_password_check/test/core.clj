(ns clj-password-check.test.core
  (:use [clj-password-check.core] :reload)
  (:use [clojure.test]))

(deftest test-combine-checkers-or
  (let [f (combine-checkers-or string? number?)]
    (are [x y] (= x y)
      true  (f "str")
      true  (f 12)
      false (f '(1 2))
      )
    )
  )

(deftest test-combine-checkers-and
  (let [f (combine-checkers-and string? (fn [s] (= \a (first s))))]
    (are [x y] (= x y)
      true  (f "abc")
      false (f "bac")
      false (f 123)
      )
    )
  )

(deftest test-contains-uppercase?
  (are [x y] (= x y)
    true  (contains-uppercase? "A")
    false (contains-uppercase? "a")
    false (contains-uppercase? "0")
    false (contains-uppercase? "!")
    false (contains-uppercase? "")
    )
  )

(deftest test-contains-lowercase?
  (are [x y] (= x y)
    false (contains-lowercase? "A")
    true  (contains-lowercase? "a")
    false (contains-lowercase? "0")
    false (contains-lowercase? "!")
    false (contains-lowercase? "")
    )
  )

(deftest test-contains-alphabet?
  (are [x y] (= x y)
    true  (contains-alphabet? "A")
    true  (contains-alphabet? "a")
    false (contains-alphabet? "0")
    false (contains-alphabet? "!")
    false (contains-alphabet? "")
    )
  )

(deftest test-contains-number?
  (are [x y] (= x y)
    false (contains-number? "A")
    false (contains-number? "a")
    true  (contains-number? "0")
    false (contains-number? "!")
    false (contains-number? "")
    )
  )

(deftest test-length-range
  (let [f (length-range 3 4)
        g (length-range 3)
        ]
    (are [x y] (= x y)
      false (f "")
      false (f "aa")
      true  (f "aaa")
      true  (f "aaaa")
      false (f "aaaaa")
      false (g "")
      false (g "aa")
      true  (g "aaa")
      true  (g "aaaa")
      true  (g (apply str (take 10000 (repeat "a"))))
      )
    )
  )

(deftest test-not-same-characters?
  (is (not-same-characters? "aaaaAaaa"))
  (is (not (not-same-characters? "aaaaaaaa")))
  (is (not (not-same-characters? "")))
  )

(deftest test-not-sequential-password?
  (are [x y] (= x y)
    true  (not-sequential-password? "hello")
    true  (not-sequential-password? "1253")
    false (not-sequential-password? "abcdefg")
    false (not-sequential-password? "gfedcba")
    false (not-sequential-password? "0123456")
    false (not-sequential-password? "6543210")
    true  (not-sequential-password? "0123457")
    true  (not-sequential-password? "")
    )
  )

(deftest test-contains-symbol?
  (are [x y] (= x y)
    false (contains-symbol? "A")
    false (contains-symbol? "a")
    false (contains-symbol? "0")
    true  (contains-symbol? "!")
    false (contains-symbol? "")
    )
  )

(deftest test-not-contains-multi-byte-character?
  (are [x y] (= x y)
    true  (not-contains-multi-byte-character? "hello")
    true  (not-contains-multi-byte-character? "!@#")
    false (not-contains-multi-byte-character? "はろー")
    true  (not-contains-multi-byte-character? "123")
    true  (not-contains-multi-byte-character? "")
    )
  )

(deftest test-last-checker
  (contains-alphabet? "a")
  (is (= 'contains-lowercase? @last-checker))
  (contains-alphabet? "A")
  (is (= 'contains-uppercase? @last-checker))

  (let [f (combine-checkers not-blank? (length-range 2 4) not-same-characters? not-sequential-password?)]
    (f "")
    (is (= 'not-blank? @last-checker))
    (f "a")
    (is (= 'length-range @last-checker))
    (f "aaa")
    (is (= 'not-same-characters? @last-checker))
    (f "abcd")
    (is (= 'not-sequential-password? @last-checker))
    (f "0123456")
    (is (= 'length-range @last-checker))
    )
  )

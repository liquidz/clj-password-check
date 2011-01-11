# clj-password-check

password checker library for clojure

## Usage

combine checker functions whiech you want with "combine-checkers"

    (def my-password-checker
	  (combine-checkers not-blank? contains-alphabet? (length-range 3 5)))

"combine-checkers" function combine checker functions with AND operator
if you want to combine with OR operator, you can use "combine-checkers-or"

#### checker functions

 * not-blank?
 * contains-uppercase?
 * contains-lowercase?
 * contains-alphabet?
 * contains-symbol?
 * not-same-characters?
    * ex) (not-same-characters? "aaaa") ; false
 * not-sequential-password?
    * ex) (not-sequential-password? "abcdef") ; false
 * not-contains-multi-byte-character?
 * length-range
    * ex) (length-range 2 5) ; function to check password length 2 <= len <= 5

#### @last-checker

"@last-checker" represents last checker function name as Symbol
with this value, you can check error point

## License

Copyright (C) 2011 Masashi Iizuka

Distributed under the Eclipse Public License, the same as Clojure.

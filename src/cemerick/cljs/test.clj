(ns 
  ^{:author "Stuart Sierra, with contributions and suggestions by 
  Chas Emerick, Allen Rohner, and Stuart Halloway",
     :doc "A unit testing framework.

   NOTE: This documentation is copied verbatim from clojure.test,
   and describes behaviour that does not apply to this ClojureScript port.
   Proper documentation to follow. :-P

   ASSERTIONS

   The core of the library is the \"is\" macro, which lets you make
   assertions of any arbitrary expression:

   (is (= 4 (+ 2 2)))
   (is (instance? Integer 256))
   (is (.startsWith \"abcde\" \"ab\"))

   You can type an \"is\" expression directly at the REPL, which will
   print a message if it fails.

       user> (is (= 5 (+ 2 2)))

       FAIL in  (:1)
       expected: (= 5 (+ 2 2))
         actual: (not (= 5 4))
       false

   The \"expected:\" line shows you the original expression, and the
   \"actual:\" shows you what actually happened.  In this case, it
   shows that (+ 2 2) returned 4, which is not = to 5.  Finally, the
   \"false\" on the last line is the value returned from the
   expression.  The \"is\" macro always returns the result of the
   inner expression.

   There are two special assertions for testing exceptions.  The
   \"(is (thrown? c ...))\" form tests if an exception of class c is
   thrown:

   (is (thrown? ArithmeticException (/ 1 0))) 

   \"(is (thrown-with-msg? c re ...))\" does the same thing and also
   tests that the message on the exception matches the regular
   expression re:

   (is (thrown-with-msg? ArithmeticException #\"Divide by zero\"
                         (/ 1 0)))

   DOCUMENTING TESTS

   \"is\" takes an optional second argument, a string describing the
   assertion.  This message will be included in the error report.

   (is (= 5 (+ 2 2)) \"Crazy arithmetic\")

   In addition, you can document groups of assertions with the
   \"testing\" macro, which takes a string followed by any number of
   assertions.  The string will be included in failure reports.
   Calls to \"testing\" may be nested, and all of the strings will be
   joined together with spaces in the final report, in a style
   similar to RSpec <http://rspec.info/>

   (testing \"Arithmetic\"
     (testing \"with positive integers\"
       (is (= 4 (+ 2 2)))
       (is (= 7 (+ 3 4))))
     (testing \"with negative integers\"
       (is (= -4 (+ -2 -2)))
       (is (= -1 (+ 3 -4)))))

   Note that, unlike RSpec, the \"testing\" macro may only be used
   INSIDE a \"deftest\" or \"with-test\" form (see below).


   DEFINING TESTS

   There are two ways to define tests.  The \"with-test\" macro takes
   a defn or def form as its first argument, followed by any number
   of assertions.  The tests will be stored as metadata on the
   definition.

   (with-test
       (defn my-function [x y]
         (+ x y))
     (is (= 4 (my-function 2 2)))
     (is (= 7 (my-function 3 4))))

   As of Clojure SVN rev. 1221, this does not work with defmacro.
   See http://code.google.com/p/clojure/issues/detail?id=51

   The other way lets you define tests separately from the rest of
   your code, even in a different namespace:

   (deftest addition
     (is (= 4 (+ 2 2)))
     (is (= 7 (+ 3 4))))

   (deftest subtraction
     (is (= 1 (- 4 3)))
     (is (= 3 (- 7 4))))

   This creates functions named \"addition\" and \"subtraction\", which
   can be called like any other function.  Therefore, tests can be
   grouped and composed, in a style similar to the test framework in
   Peter Seibel's \"Practical Common Lisp\"
   <http://www.gigamonkeys.com/book/practical-building-a-unit-test-framework.html>

   (deftest arithmetic
     (addition)
     (subtraction))

   The names of the nested tests will be joined in a list, like
   \"(arithmetic addition)\", in failure reports.  You can use nested
   tests to set up a context shared by several tests.


   RUNNING TESTS

   Run tests with the function \"(run-tests namespaces...)\":

   (run-tests 'your.namespace 'some.other.namespace)

   If you don't specify any namespaces, the current namespace is
   used.  To run all tests in all namespaces, use \"(run-all-tests)\".

   By default, these functions will search for all tests defined in
   a namespace and run them in an undefined order.  However, if you
   are composing tests, as in the \"arithmetic\" example above, you
   probably do not want the \"addition\" and \"subtraction\" tests run
   separately.  In that case, you must define a special function
   named \"test-ns-hook\" that runs your tests in the correct order:

   (defn test-ns-hook []
     (arithmetic))

   Note: test-ns-hook prevents execution of fixtures (see below).


   FIXTURES

   Fixtures allow you to run code before and after tests, to set up
   the context in which tests should be run.

   A fixture is just a function that calls another function passed as
   an argument.  It looks like this:

   (defn my-fixture [f]
      Perform setup, establish bindings, whatever.
     (f)  Then call the function we were passed.
      Tear-down / clean-up code here.
    )

   Fixtures are attached to namespaces in one of two ways.  \"each\"
   fixtures are run repeatedly, once for each test function created
   with \"deftest\" or \"with-test\".  \"each\" fixtures are useful for
   establishing a consistent before/after state for each test, like
   clearing out database tables.

   \"each\" fixtures can be attached to the current namespace like this:
   (use-fixtures :each fixture1 fixture2 ...)
   The fixture1, fixture2 are just functions like the example above.
   They can also be anonymous functions, like this:
   (use-fixtures :each (fn [f] setup... (f) cleanup...))

   The other kind of fixture, a \"once\" fixture, is only run once,
   around ALL the tests in the namespace.  \"once\" fixtures are useful
   for tasks that only need to be performed once, like establishing
   database connections, or for time-consuming tasks.

   Attach \"once\" fixtures to the current namespace like this:
   (use-fixtures :once fixture1 fixture2 ...)

   Note: Fixtures and test-ns-hook are mutually incompatible.  If you
   are using test-ns-hook, fixture functions will *never* be run.


   SAVING TEST OUTPUT TO A FILE

   All the test reporting functions write to the var *test-out*.  By
   default, this is the same as *out*, but you can rebind it to any
   PrintWriter.  For example, it could be a file opened with
   clojure.java.io/writer.


   EXTENDING TEST-IS (ADVANCED)

   You can extend the behavior of the \"is\" macro by defining new
   methods for the \"assert-expr\" multimethod.  These methods are
   called during expansion of the \"is\" macro, so they should return
   quoted forms to be evaluated.

   You can plug in your own test-reporting framework by rebinding
   the \"report\" function: (report event)

   The 'event' argument is a map.  It will always have a :type key,
   whose value will be a keyword signaling the type of event being
   reported.  Standard events with :type value of :pass, :fail, and
   :error are called when an assertion passes, fails, and throws an
   exception, respectively.  In that case, the event will also have
   the following keys:

     :expected   The form that was expected to be true
     :actual     A form representing what actually occurred
     :message    The string message given as an argument to 'is'

   The \"testing\" strings will be a list in \"*testing-contexts*\", and
   the vars being tested will be a list in \"*testing-vars*\".

   Your \"report\" function should wrap any printing calls in the
   \"with-test-out\" macro, which rebinds *out* to the current value
   of *test-out*.

   For additional event types, see the examples in the code.
"}
  cemerick.cljs.test
  (:require cljs.compiler
            [cljs.analyzer :refer (*cljs-ns*)]
            [clojure.template :as temp]))

;; TODO seems like there's no reason to expose this for cljs; you're not
;; likely to be shipping .cljs files into production
(defonce ^:dynamic ^:private
  ^{:doc "True by default.  If set to false, no test functions will
   be created by deftest, set-test, or with-test.  Use this to omit
   tests when compiling or loading production code."
    :added "1.1"}
  *load-tests* true)

(defmacro with-test-out
  "Runs body with *print-fn* bound to the value of *test-print-fn* is bound non-nil."
  {:added "1.1"}
  [& body]
  `(binding [cljs.core/*print-fn* (or *test-print-fn* cljs.core/*print-fn*)]
     ~@body))

;;; UTILITIES FOR ASSERTIONS

(defn function?
  "Returns true if argument is a function or a symbol that resolves to
  a function (not a macro)."
  {:added "1.1"}
  [x]
  (and (symbol? x) (not (.startsWith (name x) "."))))

(defn assert-predicate
  "Returns generic assertion code for any functional predicate.  The
  'expected' argument to 'report' will contains the original form, the
  'actual' argument will contain the form with all its sub-forms
  evaluated.  If the predicate returns false, the 'actual' form will
  be wrapped in (not...)."
  {:added "1.1"}
  [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           result# (apply ~pred values#)]
       (if result#
         (do-report {:type :pass, :message ~msg,
                  :expected '~form, :actual (cons ~pred values#)})
         (do-report {:type :fail, :message ~msg,
                  :expected '~form, :actual (list '~'not (cons '~pred values#))}))
       result#)))

(defn assert-any
  "Returns generic assertion code for any test, including macros, Java
  method calls, or isolated symbols."
  {:added "1.1"}
  [msg form]
  `(let [value# ~form]
     (if value#
       (do-report {:type :pass, :message ~msg,
                :expected '~form, :actual value#})
       (do-report {:type :fail, :message ~msg,
                :expected '~form, :actual value#}))
     value#))



;;; ASSERTION METHODS

;; You don't call these, but you can add methods to extend the 'is'
;; macro.  These define different kinds of tests, based on the first
;; symbol in the test expression.

(defmulti assert-expr 
  (fn [msg form]
    (cond
      (nil? form) :always-fail
      (seq? form) (first form)
      :else :default)))

(defmethod assert-expr :always-fail [msg form]
  ;; nil test: always fail
  `(do-report {:type :fail, :message ~msg}))

(defmethod assert-expr :default [msg form]
  (if (and (seq? form) (function? (first form)))
    (assert-predicate msg form)
    (assert-any msg form)))

(defmethod assert-expr 'instance? [msg form]
  ;; Test if x is an instance of y.
  `(let [object# ~(nth form 2)]
     (let [result# (instance? ~(nth form 1) object#)]
       (if result#
         (do-report {:type :pass, :message ~msg,
                  :expected '~form, :actual (type object#)})
         (do-report {:type :fail, :message ~msg,
                  :expected '~form, :actual (type object#)}))
       result#)))

(defmethod assert-expr 'thrown? [msg form]
  ;; (is (thrown? c expr))
  ;; Asserts that evaluating expr throws an exception of class c.
  ;; Returns the exception thrown.
  (let [klass (second form)
        body (nthnext form 2)]
    `(try ~@body
          (do-report {:type :fail, :message ~msg,
                   :expected '~form, :actual nil})
          (catch ~klass e#
            (do-report {:type :pass, :message ~msg,
                     :expected '~form, :actual e#})
            e#))))

(defmethod assert-expr 'thrown-with-msg? [msg form]
  ;; (is (thrown-with-msg? c re expr))
  ;; Asserts that evaluating expr throws an exception of class c.
  ;; Also asserts that the message string of the exception matches
  ;; (with re-find) the regular expression re.
  (let [klass (nth form 1)
        re (nth form 2)
        body (nthnext form 3)]
    `(try ~@body
          (do-report {:type :fail, :message ~msg, :expected '~form, :actual nil})
          (catch ~klass e#
            (let [m# (.-message e#)]
              (if (re-find ~re m#)
                (do-report {:type :pass, :message ~msg,
                         :expected '~form, :actual e#})
                (do-report {:type :fail, :message ~msg,
                         :expected '~form, :actual e#})))
            e#))))


(defmacro try-expr
  "Used by the 'is' macro to catch unexpected exceptions.
  You don't call this."
  {:added "1.1"}
  [msg form]
  `(try ~(assert-expr msg form)
        (catch js/Error t#
          (do-report {:type :error, :message ~msg,
                      :expected '~form, :actual t#}))))



;;; ASSERTION MACROS

;; You use these in your tests.

(defmacro is
  "Generic assertion macro.  'form' is any predicate test.
  'msg' is an optional message to attach to the assertion.
  
  Example: (is (= 4 (+ 2 2)) \"Two plus two should be 4\")

  Special forms:

  (is (thrown? c body)) checks that an instance of c is thrown from
  body, fails if not; then returns the thing thrown.

  (is (thrown-with-msg? c re body)) checks that an instance of c is
  thrown AND that the message on the exception matches (with
  re-find) the regular expression re."
  {:added "1.1"} 
  ([form] `(is ~form nil))
  ([form msg] `(try-expr ~msg ~form)))

(defmacro are
  "Checks multiple assertions with a template expression.
  See clojure.template/do-template for an explanation of
  templates.

  Example: (are [x y] (= x y)  
                2 (+ 1 1)
                4 (* 2 2))
  Expands to: 
           (do (is (= 2 (+ 1 1)))
               (is (= 4 (* 2 2))))

  Note: This breaks some reporting features, such as line numbers."
  {:added "1.1"}
  [argv expr & args]
  (if (or
       ;; (are [] true) is meaningless but ok
       (and (empty? argv) (empty? args))
       ;; Catch wrong number of args
       (and (pos? (count argv))
            (pos? (count args))
            (zero? (mod (count args) (count argv)))))
    `(temp/do-template ~argv (is ~expr) ~@args)
    (throw (IllegalArgumentException. "The number of args doesn't match are's argv."))))

(defmacro testing
  "Adds a new string to the list of testing contexts.  May be nested,
  but must occur inside a test function (deftest)."
  {:added "1.1"}
  [string & body]
  `(binding [*testing-contexts* (conj *testing-contexts* ~string)]
     ~@body))

;;; DEFINING TESTS

(defn- munged-symbol
  [& strs]
  (symbol (cljs.compiler/munge (apply str strs))))

;; metadata on functions (esp top level fns?) in cljs is bizarre;
;; the result of with-meta isn't a js/Function, but it is an IFn?
;; TODO might be better to just define another top-level and register
;; that instead
(defmacro set-test
  [name & body]
  (when *load-tests*
    `(do
       (set! ~name (vary-meta ~name assoc
                              :name '~name
                              :test (fn ~(symbol (str name "-test")) [] ~@body)))
       (register-test! '~*cljs-ns* ~(munged-symbol *cljs-ns* "." name))
       ~name)))

(defmacro with-test
  "Takes any definition form (that returns a Var) as the first argument.
  Remaining body goes in the :test metadata function for that Var.

  When *load-tests* is false, only evaluates the definition, ignoring
  the tests."
  {:added "1.1"}
  [definition & body]
  `(do ~definition (set-test ~(second definition) ~@body)))

(defmacro deftest
  "Defines a test function with no arguments.  Test functions may call
  other tests, so tests may be composed.  If you compose tests, you
  should also define a function named test-ns-hook; run-tests will
  call test-ns-hook instead of testing all vars.

  Note: Actually, the test body goes in the :test metadata on the var,
  and the real function (the value of the var) calls test-var on
  itself.

  When *load-tests* is false, deftest is ignored."
  {:added "1.1"}
  [name & body]
  (when *load-tests*
    `(do
       (defn ~name [] (test-var ~(munged-symbol *cljs-ns* "." name)))
       (set-test ~name ~@body))))

(defmacro deftest-
  "Like deftest but creates a private var."
  {:added "1.1"}
  [name & body]
  `(deftest ~(vary-meta name assoc :private true) ~@body))

(defmacro deftesthook
  [name & body]
  `(do
     (defn ~name ~@body)
     (register-test-ns-hook! '~*cljs-ns* ~(munged-symbol *cljs-ns* "." name))
     ~name))

;;; DEFINING FIXTURES

(defmacro use-fixtures
  "Wrap test runs in a fixture function to perform setup and
  teardown. Using a fixture-type of :each wraps every test
  individually, while :once wraps the whole run in a single function."
  [fixture-type & args]
  `(register-fixtures! '~(munged-symbol *cljs-ns*) ~fixture-type ~@args))

;;; RUNNING TESTS; (many more options available in test.cljs)

(defmacro run-tests
  "Runs all tests in the given namespaces; prints results.
  Defaults to current namespace if none given.  Returns a map
  summarizing test results."
  {:added "1.1"}
  ([] `(run-tests* '~*cljs-ns*))
  ([& namespaces] `(run-tests* ~@namespaces)))
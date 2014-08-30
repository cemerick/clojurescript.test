// reusable phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
var fs = require('fs');
var sys = require('system');

var f1 = "(function () { "
    + "try { ";
var f2 = "; } catch (err) { e = new Error('";
var f3 = "' + "
    + "' is not a file, and evaluating as an expression threw: \"' + "
    + "err.message + '\"'); throw e; }"
    + " })";

for (var i = 1; i < sys.args.length; i++) {
    if (fs.exists(sys.args[i])) {
        if (!p.injectJs(sys.args[i])) throw new Error("Failed to inject " + sys.args[i]);
    } else {
        p.evaluateJavaScript(f1 + sys.args[i] + f2 + sys.args[i] + f3);
    }
}

p.onError = function(msg) {
  var haveCljsTest = p.evaluate(function() {
    return (typeof cemerick !== "undefined" &&
        typeof cemerick.cljs !== "undefined" &&
        typeof cemerick.cljs.test !== "undefined" &&
        typeof cemerick.cljs.test.run_all_tests === "function");
  });

  if  (haveCljsTest) {
      console.error(msg);
  } else {
      var messageLines = [
          "",
          "ERROR: cemerick.cljs.test was not required.",
          "",
          "You can resolve this issue by ensuring [cemerick.cljs.test] appears",
          "in the :require clause of your test suite namespaces.",
          "Also make sure that your build has actually included any test files.",
          ""
      ];
      console.error(messageLines.join("\n"));
      phantom.exit(1);
  }
};

p.onConsoleMessage = function (x) {
  var line = x.toString();
  if (line !== "[NEWLINE]") {
    console.log(line.replace(/\[NEWLINE\]/g, "\n"));
  }
};

p.evaluate(function () {
  cemerick.cljs.test.set_print_fn_BANG_(function(x) {
    console.log(x.replace(/\n/g, "[NEWLINE]")); // since console.log *itself* adds a newline
  });
});

// p.evaluate is sandboxed, can't ship closures across;
// so, a bit of a hack, better than polling :-P
var exitCodePrefix = "phantom-exit-code:";
p.onAlert = function (msg) {
  var exit = msg.replace(exitCodePrefix, "");
  if (msg != exit) phantom.exit(parseInt(exit));
};

p.evaluate(function (exitCodePrefix) {
  var results = cemerick.cljs.test.run_all_tests();
  //console.log(results);
  cemerick.cljs.test.on_testing_complete(results, function () {
      window.alert(exitCodePrefix +
        (cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1));
  });
}, exitCodePrefix);

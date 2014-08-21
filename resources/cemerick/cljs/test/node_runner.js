var path = require("path"),
  fs = require("fs"),
  args = process.argv.slice(2);

var haveCljsTest = function () {
    return (typeof cemerick !== "undefined" &&
        typeof cemerick.cljs !== "undefined" &&
        typeof cemerick.cljs.test !== "undefined" &&
        typeof cemerick.cljs.test.run_all_tests === "function");
};

var failIfCljsTestUndefined = function () {
    if (!haveCljsTest()) {
        var messageLines = [
            "",
            "ERROR: cemerick.cljs.test was not required.",
            "",
            "You can resolve this issue by ensuring [cemerick.cljs.test] appears",
            "in the :require clause of your test suite namespaces.",
            "Also make sure that your build has actually included any test files.",
            "",
            "Also remember that Node.js can be only used with simple/advanced ",
            "optimizations, not with none/whitespace.",
            ""
        ];
        console.error(messageLines.join("\n"));
        process.exit(1);
    }
}

args.forEach(function (arg) {
    var file = path.join(process.cwd(), arg);
    if (fs.existsSync(file)) {
      try {
        // using eval instead of require here so that `this` is the "real"
        // top-level scope, not the module
        var content = fs.readFileSync(file, {encoding: "UTF-8"});
        eval("(function () {" + content + "})()");
      } catch (e) {
        console.log("Error in file: \"" + file + "\"");
        console.log(e);
      }
    } else {
      try {
        eval("(function () {" + arg + "})()");
      } catch (e) {
        console.log("Could not evaluate expression: \"" + arg + "\"");
        console.log(e);
      }
    }
});

failIfCljsTestUndefined(); // check this before trying to call set_print_fn_BANG_

cemerick.cljs.test.set_print_fn_BANG_(function(x) {
    // since console.log *itself* adds a newline 
    var x = x.replace(/\n$/, "");
    if (x.length > 0) console.log(x);
});

var success = (function() {
    var results = cemerick.cljs.test.run_all_tests();
    cemerick.cljs.test.on_testing_complete(results, function () {
        process.exit(cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1);
    });
})();

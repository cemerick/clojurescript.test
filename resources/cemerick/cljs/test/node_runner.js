var path = require("path");
var fs = require("fs");
var args = process.argv.slice(2);

var haveCljsTest = function () {
  return (typeof cemerick !== "undefined" &&
	  typeof cemerick.cljs !== "undefined" &&
	  typeof cemerick.cljs.test !== "undefined" &&
	  typeof cemerick.cljs.test.run_all_tests === "function");
};

args.forEach(function (arg) {
    var file = path.join(process.cwd(), arg);
    if (fs.existsSync(file)) {
      try {
        // Using eval instead of require here so that `this` is the "real"
        // top-level scope, not the module.
        eval("(function () {" + fs.readFileSync(file, {encoding: "UTF-8"}) + "})()");
      } catch (e) {
	if (haveCljsTest()) {
          console.error("Error in file: \"" + file + "\"");
          console.error(e);
	} else {
	  var messageLines = [
	    "",
	    "ERROR: cemerick.cljs.test was not required.",
	    "",
	    "You can resolve this issue by ensuring [cemerick.cljs.test] appears",
	    "in the :require clause of your test suite namespaces.",
	    ""
	  ];
	  console.log(messageLines.join("\n"));
	  process.exit(1);
	}
      }
    } else {
      try {
        eval("(function () {" + arg + "})()");
      } catch (e) {
        console.error("Could not evaluate expression: \"" + arg + "\"");
        console.error(e);
      }
    }
});

cemerick.cljs.test.set_print_fn_BANG_(function(x) {
  // since console.log *itself* adds a newline
  var x = x.replace(/\n$/, "");
  if (x.length > 0) console.log(x);
});

var success = (function() {
    var results = cemerick.cljs.test.run_all_tests();
    return cemerick.cljs.test.successful_QMARK_(results);
})();

process.exit(success ? 0 : 1);

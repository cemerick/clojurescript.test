var haveCljsTest = function () {
  return (typeof cemerick !== "undefined" &&
	  typeof cemerick.cljs !== "undefined" &&
	  typeof cemerick.cljs.test !== "undefined" &&
	  typeof cemerick.cljs.test.run_all_tests === "function");
};

arguments.forEach(function (arg) {
    print(arg);
    if (new java.io.File(arg).exists()) {
      try {
        load(arg);
      } catch (e) {
	if (haveCljsTest()) {
	  print("Error in file: \"" + arg + "\"");
	  print(e);
	} else {
	  var messageLines = [
	    "",
	    "ERROR: cemerick.cljs.test was not required.",
	    "",
	    "You can resolve this issue by ensuring [cemerick.cljs.test] appears",
	    "in the :require clause of your test suite namespaces.",
	    ""
	  ];
	  print(messageLines.join("\n"));
	  java.lang.System.exit(1);
	}
      }
    } else {
      try {
        eval("(function () {" + arg + "})()");
      } catch (e) {
        print("Could not evaluate expression: \"" + arg + "\"");
        print(e);
      }
    }
});

cemerick.cljs.test.set_print_fn_BANG_(function(x) {
    // since console.log *itself* adds a newline
    var x = x.replace(/\n$/, "");
    if (x.length > 0) print(x);
});

var results = cemerick.cljs.test.run_all_tests();
var success = cemerick.cljs.test.successful_QMARK_(results);

java.lang.System.exit(success ? 0 : 1);

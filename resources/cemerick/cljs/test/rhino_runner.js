arguments.forEach(function (arg) {
    print(arg)
    if (new java.io.File(arg).exists()) {
      try {
        load(arg);
      } catch (e) {
        print("Error in file: \"" + arg + "\"");
        print(e);
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

var path = require("path"),
  fs = require("fs"),
  args = process.argv.slice(2);

args.forEach(function (arg) {
    var file = path.join(process.cwd(), arg);
    if (fs.existsSync(file)) {
      try {
        require(file);
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

cemerick.cljs.test.set_print_fn_BANG_(console.log);

var success = (function() {
    var results = cemerick.cljs.test.run_all_tests();
    return cemerick.cljs.test.successful_QMARK_(results);
})();

process.exit(success ? 0 : 1);

var setTimeout, clearTimeout, setInterval, clearInterval;

(function () {
    var executor = Java.type("java.util.concurrent.Executors").newScheduledThreadPool(1);
    var counter = 1;
    var ids = {};

    var RunnableExtender = Java.extend(Java.type("java.lang.Runnable"));

    setTimeout = function (fn,delay) {
        var id = counter++;
	var runnable = new RunnableExtender() { run: fn }
        ids[id] = executor.schedule(runnable, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
        return id;
    }

    clearTimeout = function (id) {
        ids[id].cancel(false);
        executor.purge();
        delete ids[id];
    }

    setInterval = function (fn,delay) {
        var id = counter++;
	var runnable = new RunnableExtender() { run: fn }
        ids[id] = executor.scheduleAtFixedRate(runnable, delay, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
        return id;
    }

    clearInterval = clearTimeout;

})()

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
            ""
        ];
        print(messageLines.join("\n"));
        java.lang.System.exit(1);
    }
}

arguments.forEach(function (arg) {
    if (new java.io.File(arg).exists()) {
        try {
            load(arg);
        } catch (e) {
            failIfCljsTestUndefined();
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

failIfCljsTestUndefined(); // check this before trying to call set_print_fn_BANG_

cemerick.cljs.test.set_print_fn_BANG_(function(x) {
    // since console.log *itself* adds a newline
    var x = x.replace(/\n$/, "");
    if (x.length > 0) print(x);
});

try {
  var results = cemerick.cljs.test.run_all_tests();
 } catch (e) {
  e.printStackTrace();
  throw(e); // propagate exception further
 }

cemerick.cljs.test.on_testing_complete(results, function () {
    java.lang.System.exit(cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1);
});
/* wait for the exit to happen */
var Thread = Java.type("java.lang.Thread");
while (true) Thread.yield();

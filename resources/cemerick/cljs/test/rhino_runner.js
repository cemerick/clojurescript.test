var setTimeout, clearTimeout, setInterval, clearInterval;

(function () {
    var executor = new java.util.concurrent.Executors.newScheduledThreadPool(1);
    var counter = 1;
    var ids = {};

    setTimeout = function (fn,delay) {
        var id = counter++;
        var runnable = new JavaAdapter(java.lang.Runnable, {run: fn});
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
        var runnable = new JavaAdapter(java.lang.Runnable, {run: fn});
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
};

function loadTestData(p) {
    var loadFile = function (file) {
        var str = readFile(file, 'utf8').replace(/[\"\'\\\t\v\b\f\n\r]/gm,"\\$&");
        eval("(function() {\
             if (!this.cljs_test_data) this.cljs_test_data = {};\
             this.cljs_test_data[\"" + file + "\"] = \"" + str + "\"; })()");
    };
    var f = new java.io.File(p);
    if (f.isDirectory()) {
        f.listFiles().forEach(function(f) {
            loadFile(f);
        });
    } else {
        loadFile(p);
    }
}

arguments.forEach(function (arg) {
   var m = arg.match(/^--test-data=(.*)$/);
   if (new java.io.File(arg).exists()) {
        try {
            load(arg);
        } catch (e) {
            failIfCljsTestUndefined();
            print("Error in file: \"" + arg + "\"");
            print(e);
        }
    } else if (m && m.length > 1) {
        loadTestData(m[1]);
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

var results = cemerick.cljs.test.run_all_tests();
cemerick.cljs.test.on_testing_complete(results, function () {
    java.lang.System.exit(cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1);
});

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

arguments.forEach(function (arg) {
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
cemerick.cljs.test.on_testing_complete(results, function () {
    java.lang.System.exit(cemerick.cljs.test.successful_QMARK_(results) ? 0 : 1);
});

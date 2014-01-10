// reusable phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
var fs = require('fs');
var sys = require('system');
for (var i = 1; i < sys.args.length; i++) {
    if (fs.exists(sys.args[i])) {
        if (!p.injectJs(sys.args[i])) throw new Error("Failed to inject " + sys.args[i]);
    } else {
        p.evaluateJavaScript("(function () { " + sys.args[i] + ";" + " })");
    }
}

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

// In order to support async tests we use onClosing handler.
// Tests will call window.close() when finished and then
// onClosing handler will be called that will exit phantom process.
// We can't exit phantom from page.
p.onClosing = function() {
    // Use title of a page as indicator whether tests
    // ran successfull or not.
    var success = p.title === "Hooray!";
    // For some reason we cannot call phantom.exit immediately.
    // Add small timeout.
    setTimeout(function() {
        phantom.exit(success ? 0 : 1);
    }, 100);
};

p.evaluate(function () {
    cemerick.cljs.test.run_all_tests(function(results) {
        console.log(results)
        if (cemerick.cljs.test.successful_QMARK_(results)) {
            // Mark that test ran successfully.
            // It will be read by p.onClosing handler.
            document.title = "Hooray!";
        }
        // Will call p.onClosing handler.
        window.close();
    });
});


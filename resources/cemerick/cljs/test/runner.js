// reusable slimerjs/phantomjs script for running clojurescript.test tests
// see http://github.com/cemerick/clojurescript.test for more info

var p = require('webpage').create();
var fs = require('fs');
var sys = require('system');

// this craziness works around inscrutable JS context issues when tests being
// run use iframes and such; rather than injecting or eval'ing test scripts and
// expressions, dump them all into a static HTML file and everything will be
// guaranteed to work.

var html = "";
var pagePath = sys.args[0] + ".html";

for (var i = 1; i < sys.args.length; i++) {
    var src;

    if (fs.exists(sys.args[i])) {
	src = fs.read(sys.args[i]);
    } else {
	if (sys.args[i].match(/\.js$/)) console.log("WARNING: additional cljsbuild :test-command argument looks like a filename, but file does not exist, including as JavaScript expression: " + sys.args[i]);
	src = sys.args[i];
    }

    html += "<script>//<![CDATA[\n" + src + "\n//]]></script>";
}

html = "<html><head>" + html + "</head><body></body></html>";
fs.write(pagePath, html, 'w');

p.open("file://" + pagePath, function () {
    fs.remove(pagePath);

    p.onError = function(msg, trace) {
	var haveCljsTest = p.evaluate(function() {
	    return (typeof cemerick !== "undefined" &&
		    typeof cemerick.cljs !== "undefined" &&
		    typeof cemerick.cljs.test !== "undefined" &&
		    typeof cemerick.cljs.test.run_all_tests === "function");
	});

	console.error(msg);
	console.error(trace.map(function(t) {
		return ' -> ' + t.file + ': ' + t.line + (t.function ? ' (in function "' + t.function +'")' : '');
	}).join('\n'));

	if (!haveCljsTest) {
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

    p.onCallback = function (x) {
	var line = x.toString();
	if (line !== "[NEWLINE]") {
	    console.log(line.replace(/\[NEWLINE\]/g, "\n"));
	}
    };

    p.evaluate(function () {
	cemerick.cljs.test.set_print_fn_BANG_(function(x) {
	    // using callPhantom to work around https://github.com/laurentj/slimerjs/issues/223
	    window.callPhantom(x.replace(/\n/g, "[NEWLINE]")); // since console.log *itself* adds a newline
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

});

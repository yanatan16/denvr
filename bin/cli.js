#!/usr/bin/env node
try {
    require("source-map-support").install();
} catch(err) {
}
require("../js/goog/bootstrap/nodejs")
require("./denver")
require("./js/denver/cli")
denver.cli._main();

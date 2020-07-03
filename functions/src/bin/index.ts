import functions = require('firebase-functions');

const runServer = process.env.RUN_SERVER || functions.config().execution?.run_server;
const runDaemon = process.env.RUN_DAEMON || functions.config().execution?.run_daemon;

if (runServer === undefined && runDaemon === undefined) {
  exports.www = require('./www');
  exports.daemon = require('./daemon');
} else {
  if (runServer)
    exports.www = require('./www');
  if (runDaemon)
    exports.daemon = require('./daemon');
}
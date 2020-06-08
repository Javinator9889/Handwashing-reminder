const runServer = process.env.RUN_SERVER;
const runDaemon = process.env.RUN_DAEMON;

if (runServer === undefined && runDaemon === undefined) {
  exports.www = require('./www');
  exports.daemon = require('./daemon');
} else {
  if (runServer)
    exports.www = require('./www');
  if (runDaemon)
    exports.daemon = require('./daemon');
}
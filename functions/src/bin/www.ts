const app = require('../app');
import * as http from 'http';
import functions = require("firebase-functions");


/**
 * Get port from environment and store in Express
 */
const port = normalizePort(process.env.PORT || '3000');

/**
 * Create the http server
 */
const server = http.createServer(app);

/**
 * Listen on a provided port, on all network interfaces
 */
server.on('error', onError);
server.on('listening', onListening);
if (process.env.RUN_SERVER)
  exports.webApi = functions.https.onRequest(app);

/**
 * Normalize a port into a number, string or false
 * @param val the port
 */
function normalizePort(val) {
  const parsedPort = parseInt(val, 10);

  if (isNaN(parsedPort)) {
    // named pipe
    return val;
  }

  if (parsedPort >= 0) {
    // port number
    return parsedPort;
  }

  return false;
}

/**
 * Event listener for HTTP server "error" event.
 */

function onError(error) {
  if (error.syscall !== 'listen') {
    throw error;
  }

  const bind = typeof port === 'string'
    ? 'Pipe ' + port
    : 'Port ' + port;

  // handle specific listen errors with friendly messages
  switch (error.code) {
    case 'EACCES':
      console.error(bind + ' requires elevated privileges');
      process.exit(1);
      break;
    case 'EADDRINUSE':
      console.error(bind + ' is already in use');
      process.exit(1);
      break;
    default:
      throw error;
  }
}

/**
 * Event listener for HTTP server "listening" event.
 */

function onListening() {
  const addr = server.address();
  const bind = typeof addr === 'string'
    ? 'pipe ' + addr
    : 'port ' + addr.port;
  console.log('Listening on ' + bind);
}

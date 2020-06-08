import updater = require('../models/updater');
import functions = require('firebase-functions');


updater.initialize()
  .then(_ => updater.scheduleUpdates());

process.on('SIGINT', () => {
  updater.stopScheduling()
    .then(process.exit(0));
});

exports.updater = functions.https.onRequest((req, resp) => resp.sendStatus(200));

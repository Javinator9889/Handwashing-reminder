import updater = require('../models/updater');
import functions = require('firebase-functions');


updater.initialize()
  .then(_ => updater.scheduleUpdates()
    .catch(err => console.warn(`Error while scheduling updates - ${err}`)))
  .catch(err => {
    console.error(`Error while initializing the updater - ${err}`);
    process.exit(1);
  });

process.on('SIGINT', () => {
  updater.stopScheduling()
    .finally(process.exit(0))
    .catch(err => console.warn(`Error while finishing the schedules - ${err}`));
});

exports.updater = functions.https.onRequest(async (req, resp) => {
  resp.sendStatus(200);
});

exports.remoteConfigTrigger = functions.remoteConfig.onUpdate(updater.remoteConfigEventHandler)

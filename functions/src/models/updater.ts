import {Updater} from '../updater';
import {RemoteConfigData} from "../rcdata";
import admin = require('firebase-admin');
import properties = require('../common/properties');


const serviceAccount = require('../../handwashing-firebase-adminsdk.json');
export const firebaseApp = admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: properties.databaseURL
});
const updaters: Record<string, Updater> = {};
const remoteConfig = new RemoteConfigData(firebaseApp);
const timers = new Set<NodeJS.Timer>();
let initCalled = false;

export async function initialize() {
  try {
    console.info('Updater is being initialized');
    initCalled = true;
    const projectProperties = properties.projectProperties(firebaseApp);
    for (const language of properties.languages) {
      const terms = await remoteConfig.getSearchTermsForLanguage(language);
      updaters[language] = new Updater(
        projectProperties.database,
        `${projectProperties.collection}_${language}`,
        terms,
        projectProperties.authToken,
        language
      );
    }
    console.info('Updaters initialized')
    remoteConfig.subscribeUpdaters(updaters);
  } catch (e) {
    console.error(`Error while initializing updaters - ${e}`);
    console.error(e);
  }
}

export async function scheduleUpdates() {
  if (!initCalled)
    throw new Error('`initialize` not called');
  console.info('Updaters are scheduling updates')
  for (const language of properties.languages) {
    timers.add(updaters[language].schedule());
  }
  console.info('Schedules for updaters are done');
}

export async function stopScheduling() {
  if (!initCalled)
    throw new Error('`initialize` not called');
  console.info('Updates are being cancelled');
  for (const timer of timers) {
    clearInterval(timer);
  }
}

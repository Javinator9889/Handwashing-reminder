import {Updater} from '../updater';
import {RemoteConfigData} from "../rcdata";
import admin = require('firebase-admin');
import properties = require('../common/properties');
import {languages} from "../common/properties";


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
  remoteConfig.subscribeUpdaters(updaters);
}

export async function scheduleUpdates() {
  if (!initCalled)
    throw new Error('`initialize` not called');
  for (const language of languages) {
    timers.add(updaters[language].schedule());
  }
}

export async function stopScheduling() {
  if (!initCalled)
    throw new Error('`initialize` not called');
  for (const timer of timers) {
    clearInterval(timer);
  }
}

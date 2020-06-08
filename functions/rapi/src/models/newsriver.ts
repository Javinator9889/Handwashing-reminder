import admin = require('firebase-admin');
import functions = require("firebase-functions");
import {ProjectProperties} from "../interfaces/projectProperties";
import {Updater} from "../updater";
import {RemoteConfigData} from "../rcdata";
import {NewsriverData} from "../newsriver";


const serviceAccount = require('../../handwashing-firebase-adminsdk.json');
export const firebaseApp = admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://handwashing.firebaseio.com'
});
const languages = new Set(['es', 'en']);
const projectProperties: ProjectProperties = {
  collection: 'news',
  database: admin.firestore(),
  authToken: functions.config().newsriver.key
}
const updaters: Record<string, Updater | undefined> = {};
const remoteConfig = new RemoteConfigData(firebaseApp);
const timers = new Set<NodeJS.Timer>();
let initCalled = false;

export async function initialize() {
  initCalled = true;
  for (const language of languages) {
    const terms = await remoteConfig.getSearchTermsForLanguage(language);
    const updater = new Updater(
      projectProperties.database,
      `${projectProperties.collection}_${language}`,
      terms,
      projectProperties.authToken,
      language
    );
    console.log('Created updater object');
    updaters[language] = updater;
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

export async function newsForLanguage(language: string) {
  if (!initCalled)
    throw new Error('`initialize` not called');
  if (language !in languages)
    throw new RangeError(`invalid language "${language}"`);
  const collection = updaters[language].collection;
  const snapshot = await collection.get();
  const data = new Array<NewsriverData>();
  snapshot.forEach(item => {
    if (item.data() !== null)
      data.push(item.data() as NewsriverData)
  });
  return data;
}

export async function stopScheduling() {
  if (!initCalled)
    throw new Error('`initialize` not called');
  for (const timer of timers)
    clearInterval(timer);
}

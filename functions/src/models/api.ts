import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

import {ProjectProperties} from '../interfaces/projectProperties';
import {Dictionary} from '../interfaces/dictionary';
import {RemoteConfigData} from '../rcdata';
import {Updater} from '../updater';

class Api {
  properties: ProjectProperties;
  remoteConfig: RemoteConfigData;
  languages: Array<string>;
  updaters: Dictionary<Updater | undefined>;

  constructor(properties: ProjectProperties,
              remoteConfig: RemoteConfigData,
              languages: Array<string>) {
    this.properties = properties;
    this.remoteConfig = remoteConfig;
    this.languages = languages;
    this.updaters = {};
    for (const language in languages) {
      this.updaters[language] = undefined;
    }
  }

  async init() {
    this.languages.forEach(language => {
      this.remoteConfig.getSearchTermsForLanguage(language)
        .then(terms => {
          const updater = new Updater(
            this.properties.database,
            `${this.properties.collection}_${language}`,
            terms,
            this.properties.authToken,
            language
          );
          this.updaters[language] = updater;
          updater.schedule();
        });
    });
  }

  newsForLanguage(language: string) {
    if (language !in this.languages)
      language = 'en';
    const collection = this.updaters[language].collection;
    collection.get()
      .then(snapshot => {
        snapshot.forEach(doc => {
          console.log(`${doc.id} => ${doc.data()}`);
        });
      });
  }
}

const serviceAccount = require('../../handwashing-firebase-adminsdk.json');
const firebaseApp = admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: 'https://handwashing.firebaseio.com'
});
const properties: ProjectProperties = {
  collection: 'news',
  database: admin.firestore(),
  authToken: functions.config().newsriver.key
};
const languages = ['es', 'en'];
const remoteConfig = new RemoteConfigData(firebaseApp);

export const api = new Api(properties, remoteConfig, languages)
api.init()
  .catch(reason => console.error(`API initialization failed: ${reason}`));
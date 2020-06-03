import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

import {ProjectProperties} from '../interfaces/projectProperties';
import {RemoteConfigData} from '../rcdata';
import {Updater} from '../updater';
import {NewsriverData} from '../newsriver';

class Api {
  properties: ProjectProperties;
  remoteConfig: RemoteConfigData;
  languages: Array<string>;
  updaters: Record<string, Updater | undefined>;
  timers: Set<NodeJS.Timer>;

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
    this.timers = new Set<NodeJS.Timer>();
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
          this.timers.add(updater.schedule());
        });
    });
    this.remoteConfig.subscribeUpdaters(this.updaters);
  }

  async newsForLanguage(language: string): Promise<Array<NewsriverData>> {
    if (language ! in this.languages)
      language = 'en';
    const collection = this.updaters[language].collection;
    const snapshot = await collection.get();
    const data = Array<NewsriverData>(snapshot.size);
    snapshot.forEach(item => {
      data.push(item.data() as NewsriverData);
    });
    return data;
  }

  finish() {
    for (const timer of this.timers) {
      clearInterval(timer);
    }
  }
}

const sdkInfo = functions.config().sdk;
const firebaseApp = admin.initializeApp({
  projectId: sdkInfo.project_id,
  credential: admin.credential.cert({
    projectId: sdkInfo.project_id,
    clientEmail: sdkInfo.client_email,
    privateKey: sdkInfo.private_key
  }),
  databaseURL: 'https://handwashing.firebaseio.com'
});
const projectProperties: ProjectProperties = {
  collection: 'news',
  database: admin.firestore(),
  authToken: functions.config().newsriver.key
};
const projectLanguages = ['es', 'en'];
const remoteConfigConnector = new RemoteConfigData(firebaseApp);

export const api = new Api(projectProperties, remoteConfigConnector, projectLanguages)
api.init()
  .catch(reason => console.error(`API initialization failed: ${reason}`));
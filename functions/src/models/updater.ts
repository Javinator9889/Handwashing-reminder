import {NewsriverUpdater} from '../newsriverUpdater';
import {RemoteConfigData} from "../rcdata";
import properties = require('../common/properties');
import {TemplateVersion} from "firebase-functions/lib/providers/remoteConfig";
import {languages} from "../common/properties";
import {EventContext} from "firebase-functions";


const updaters: Record<string, NewsriverUpdater> = {};
const remoteConfig = new RemoteConfigData(properties.firebaseApp);
const timers = new Set<NodeJS.Timer>();
let initCalled = false;

export async function initialize() {
  try {
    console.info('NewsriverUpdater is being initialized');
    initCalled = true;
    const projectProperties = properties.projectProperties(properties.firebaseApp);
    for (const language of properties.languages) {
      const terms = await remoteConfig.getSearchTermsForLanguage(language);
      updaters[language] = new NewsriverUpdater(
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

export async function remoteConfigEventHandler(event: TemplateVersion, _: EventContext) {
  console.debug(`RemoteConfig values have changed - version ${event.versionNumber}`);
  for (const language of languages) {
    console.debug(`Updating search terms for language ${language}`);
    remoteConfig.updaters[language].searchTerms = await remoteConfig.getSearchTermsForLanguage(language);
  }
}

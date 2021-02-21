import {NewsriverData} from "../newsriver";
import {Database} from "../database";
import properties = require('../common/properties');


const databases: Record<string, Database> = {};
const latestResults: Record<string, Array<NewsriverData>> = {};
const lastUpdateMsForLanguage: Record<string, number> = {};
let initCalled = false;

export async function initialize() {
  initCalled = true;
  const projectProperties = properties.projectProperties(properties.firebaseApp);
  for (const language of properties.languages) {
    databases[language] = new Database(
      projectProperties.database,
      `${projectProperties.collection}_${language}`
    );
    lastUpdateMsForLanguage[language] = 0;
    latestResults[language] = null;
  }
}

export async function newsForLanguage(language: string) {
  if (!initCalled)
    throw new Error('`initialize` not called');

  if (language ! in properties.languages)
    throw new RangeError(`invalid language "${language}"`);

  if (Math.floor((Date.now() - lastUpdateMsForLanguage[language]) / 60000) <= 15)
    return latestResults[language];

  lastUpdateMsForLanguage[language] = Date.now();
  const collection = databases[language].collection;
  const snapshot = await collection.orderBy("discoverDate", "desc").get();
  const data = new Array<NewsriverData>();
  snapshot.forEach(item => {
    if (item.data() !== null)
      data.push(item.data() as NewsriverData)
  });
  latestResults[language] = data;
  return data;
}

import admin = require('firebase-admin');
import {NewsriverData} from "../newsriver";
import {Database} from "../database";
import properties = require('../common/properties');


export const firebaseApp = admin.initializeApp({
  databaseURL: properties.databaseURL
});
const databases: Record<string, Database> = {};
let initCalled = false;

export async function initialize() {
  initCalled = true;
  const projectProperties = properties.projectProperties(firebaseApp);
  for (const language of properties.languages) {
    databases[language] = new Database(
      projectProperties.database,
      `${projectProperties.collection}_${language}`
    );
  }
}

export async function newsForLanguage(language: string) {
  if (!initCalled)
    throw new Error('`initialize` not called');
  if (language ! in properties.languages)
    throw new RangeError(`invalid language "${language}"`);
  const collection = databases[language].collection;
  const snapshot = await collection.get();
  const data = new Array<NewsriverData>();
  snapshot.forEach(item => {
    if (item.data() !== null)
      data.push(item.data() as NewsriverData)
  });
  return data;
}

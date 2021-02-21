import semver = require('semver');
import admin = require('firebase-admin');
import functions = require('firebase-functions');
import { ProjectProperties } from '../interfaces/projectProperties';

export const languages = new Set<string>(['es', 'en']);

export function projectProperties(app?: admin.app.App,
                                  version: string = "1.0"): ProjectProperties {
  return semver.gte(version, '2.0') ? {
    collection: 'news',
    database: admin.firestore(app),
    authToken: functions.config().newsriver.key,
  } : {
    collection: 'newsapi',
    database: admin.firestore(app),
    authToken: functions.config().newsapi.key
  };
}

export const databaseURL = 'https://handwashing.firebaseio.com';

let serviceAccount = undefined;
if (process.env.RUNNING_LOCAL)
  serviceAccount = require('../../handwashing-firebase-adminsdk.json');

export const firebaseApp = admin.initializeApp({
  credential: process.env.RUNNING_LOCAL
      ? admin.credential.cert(serviceAccount)
      : admin.credential.applicationDefault(),
  databaseURL: databaseURL,
});

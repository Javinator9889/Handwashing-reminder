import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as firebaseHelper from 'firebase-functions-helper';
import * as express from 'express';
import * as bodyParser from 'body-parser';
import { RemoteConfigData } from './rcdata';
import { Updater } from './updater';

const serviceAccount = require('../handwashing-firebase-adminsdk.json');
const firebaseApp = admin.initializeApp(
  {
    credential: admin.credential.cert(serviceAccount),
    databaseURL: 'https://handwashing.firebaseio.com'
  }
);
const newsCollection = 'news';
const db = admin.firestore();
const authToken = functions.config().newsriver.key;
const updaters = new Set<Updater>();
const languages = new Set(['es', 'en']);
const rc = new RemoteConfigData(firebaseApp);

languages.forEach(language => {
  rc.getSearchTermsForLanguage(language)
    .then(res => {
      console.log(`Search terms for language ${language} obtained: ${res}`);
      const updater = new Updater(db, newsCollection, res, authToken, language, 1);
      updaters.add(updater);
      updater.schedule();
    });
});

const app = express();
const main = express();

main.use('/api/v1', app);
main.use(bodyParser.json());
main.use(bodyParser.urlencoded({ extended: false }));

export const webApi = functions.https.onRequest(main);

app.get('/', async (req, res) => {
  try {
    // const language = req.body['lang'];
    const doc = await firebaseHelper.firestore.createNewDocument(db, newsCollection, { 'test': 'test' });
    res.status(201).send(`Created new contact: ${doc.id}`);
  } catch (error) {
    res.status(400).send('Data must contain langauge');
  }
});

module.exports = app;

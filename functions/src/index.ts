import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import * as firebaseHelper from 'firebase-functions-helper';
import * as express from 'express';
import * as bodyParser from 'body-parser';

admin.initializeApp(functions.config().firebase);

const db = admin.firestore();
const TIME_INTERVAL = 15 * 60 * 1000;
const NEWS_URL = 'https://api.newsriver.io/v2/';

const app = express();
const main = express();

const jsonCollectionName = 'news';

main.use('/api/v1', app);
main.use(bodyParser.json());
main.use(bodyParser.urlencoded({ extended: false }));

export const webApi = functions.https.onRequest(main);

app.get('/', async (req, res) => {
  try {
    const language = req.body['lang'];
    const doc = await firebaseHelper.firestore.createNewDocument(db, jsonCollectionName, {'test': 'test'});
    res.status(201).send(`Created new contact: ${doc.id}`);
  } catch (error) {
    res.status(400).send('Data must contain langauge');
  }
});

const timerId = setInterval(() => {
  const httpRequest = new XMLHttpRequest();

  httpRequest.open('GET', NEWS_URL);
  httpRequest.send();

  httpRequest.onreadystatechange = (e) => {
    try {
      const response = JSON.parse(httpRequest.responseText);
      response
    }
  }
}, TIME_INTERVAL);

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
// export const helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


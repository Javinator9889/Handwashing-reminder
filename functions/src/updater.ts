// import { NewsriverData } from "./newsriver";
import firebaseHelper = require("firebase-functions-helper");
import XMLHttpRequest = require('xhr2');

export class Updater {
  db: FirebaseFirestore.Firestore;
  collectionName: string;
  interval: number;
  searchTerms: Array<string>;
  language: string;
  auth: string;
  url: string | undefined;

  constructor(db: FirebaseFirestore.Firestore,
    collectionName: string,
    searchTerms: Array<string>,
    auth: string,
    language: string = 'en',
    intervalMins: number = 15) {
    this.db = db;
    this.collectionName = collectionName;
    this.searchTerms = searchTerms;
    this.language = language;
    this.auth = auth;
    this.interval = intervalMins * 60 * 1000;
    this.buildURL()
      .then(url => this.url = url);
  }

  schedule(): NodeJS.Timer {
    return setInterval(() => {
      const httpRequest = new XMLHttpRequest();
      const that = this;
      console.log('Requesting news');
      while (this.url === undefined);
      console.log(`URL: ${this.url}`);
      httpRequest.open('GET', this.url);
      httpRequest.setRequestHeader('Authorization', this.auth);
      httpRequest.send();
      httpRequest.onreadystatechange = () => {
        console.log('Callback called');
        if (httpRequest.status === 200) {
          console.log('Response is OK');
          that.updateData(httpRequest.responseText);
        }
      };
      httpRequest.onerror = (error) => {
        console.log(`error ocurred during process ${error}`)
      }
    }, this.interval);
  }

  async updateData(content: string) {
    try {
      console.log(content);
      const response = JSON.parse(content);
      console.log(`Got response: ${response}`);

      return;

      response.forEach(element => {
        if (!firebaseHelper.firestore.checkDocumentExists(this.db, this.collectionName, element.id)) {
          firebaseHelper.firestore.createDocumentWithID(this.db, this.collectionName, element.id, element);
        }
      });
    } catch (error) {
      console.error(error);
    }
  }

  buildURL(): Promise<string> {
    return new Promise(resolve => {
      const parts = ['https://api.newsriver.io/v2/search?query='];
      this.searchTerms.forEach((term, i, _) => {
        if (i !== 0)
          parts.push(encodeURI(' OR '));
        parts.push(encodeURI(`title:${term} OR text:${term}`));
      });
      let language: string;
      switch (this.language) {
        case 'es': language = 'ES'; break;
        default: language = 'EN'; break;
      }
      parts.push(encodeURI(` AND language:${language}`));
      parts.push(encodeURI('&sortBy=discoverDate'));
      parts.push(encodeURI('&sortOrder=DESC'));
      parts.push(encodeURI('&limit=10'));
      resolve(parts.join(''));
    });
  }
}
import { NewsriverData } from "./newsriver";
import firebaseHelper = require("firebase-functions-helper");

class Updater {
  db: FirebaseFirestore.Firestore;
  collectionName: string;
  interval: number;
  searchTerms: Array<string>;
  language: string;
  url: string | undefined;
  auth: string;

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
      httpRequest.setRequestHeader('Authorization', this.auth);
      httpRequest.open('GET', this.url);
      httpRequest.onreadystatechange = _ => {
        this.updateData(httpRequest.responseText);
      }
    }, this.interval);
  }

  async updateData(content: string) {
    const response: Array<NewsriverData> = JSON.parse(content);
    response.forEach(element => {
      if (!firebaseHelper.firestore.checkDocumentExists(this.db, this.collectionName, element.id)) {
        firebaseHelper.firestore.createDocumentWithID(this.db, this.collectionName, element.id, element);
      }
    });
  }

  buildURL(): Promise<string> {
    return new Promise(resolve => {
      const parts = ['https://api.newsriver.io/v2/search?query='];
      this.searchTerms.forEach((term, i, _) => {
        if (i !== 0)
          parts.push[' '];
        parts.push[`title:${term} OR text:${term}`];
      });
      let language: string;
      switch(this.language) {
        case 'es': language = 'ES'; break;
        default: language = 'EN'; break;
      }
      parts.push[` AND language:${language}`];
      parts.push['&sortBy=discoverDate'];
      parts.push['&sortOrder=DESC'];
      parts.push['&limit=200'];
      resolve(parts.join(''));
    });
  }
}
import {NewsriverData} from "./newsriver";
import * as firebaseHelper from 'firebase-functions-helper';
import * as fetch from 'node-fetch';


export class Updater {
  private readonly db: FirebaseFirestore.Firestore;
  private readonly collectionName: string;
  private readonly interval: number;
  private _searchTerms: Array<string>;
  private readonly language: string;
  private readonly auth: string;
  private _url: string | undefined;

  get url(): Promise<string> {
    if (this._url === undefined)
      return this.buildURL()
        .then(url => this._url = url);
    return Promise.resolve(this._url);
  }

  get searchTerms(): Array<string> {
    return this._searchTerms;
  }

  set searchTerms(value) {
    this._searchTerms = value;
    this._url = undefined;
  }

  constructor(db: FirebaseFirestore.Firestore | null,
              collectionName: string | null,
              searchTerms: Array<string>,
              auth: string,
              language: string = 'en',
              intervalMins: number = 30) {
    this.db = db;
    this.collectionName = collectionName;
    this.searchTerms = searchTerms;
    this.language = language;
    this.auth = auth;
    this.interval = intervalMins * 60 * 1000;
    this.request()
      .then(response => {
        this.updateData(response)
          // tslint:disable-next-line:no-empty
          .catch(ignored => {
          });
      })
      .catch(err => console.warn(`Unable to update data due to exception: ${err}`));
  }

  schedule(): NodeJS.Timer {
    return setInterval(async () => {
      try {
        const response = await this.request();
        await this.updateData(response);
      } catch (e) {
        console.error(`Got error ${e} while querying data`);
      }
    }, this.interval);
  }

  async updateData(content: Array<NewsriverData>) {
    try {
      for (const element of content) {
        try {
          const exists = await firebaseHelper.firestore.checkDocumentExists(this.db, this.collectionName, element.id);
          if (!exists)
            await firebaseHelper.firestore.createDocumentWithID(this.db, this.collectionName, element.id, element);
          else
            await firebaseHelper.firestore.updateDocument(this.db, this.collectionName, element.id, element);
        } catch (err) {
          console.warn(`Error while creating/updating document - ${err}`);
        }
      }
      console.info(`Updated approximately ${content.length} element(s)`);
    } catch (error) {
      console.error(`Unhandled error ${error}`);
    }
  }

  async request(): Promise<Array<NewsriverData>> {
    const requestUrl = await this.url;
    const response = await fetch(requestUrl, {
      method: 'GET', headers: new fetch.Headers({
        'Authorization': this.auth,
        'Content-Type': 'application/json'
      })
    });
    return await response.json() as Array<NewsriverData>;
  }

  async buildURL(): Promise<string> {
    const parts = ['https://api.newsriver.io/v2/search?query='];
    this.searchTerms.forEach((term, i, _) => {
      if (i !== 0)
        parts.push(encodeURI(' OR '));
      parts.push(encodeURI(`title:${term} OR text:${term}`));
    });

    parts.push(encodeURI(` AND language:${this.language.toUpperCase()}`));
    parts.push(encodeURI('&sortBy=discoverDate'));
    parts.push(encodeURI('&sortOrder=DESC'));
    parts.push(encodeURI('&limit=100'));

    return parts.join('');
  }
}
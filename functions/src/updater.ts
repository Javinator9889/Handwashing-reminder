import {NewsriverData} from "./newsriver";
import * as firebaseHelper from 'firebase-functions-helper';
import {AxiosInstance, default as axios} from "axios";
import * as https from "https";


export class Updater {
  private readonly db: FirebaseFirestore.Firestore;
  private readonly collectionName: string;
  private readonly interval: number;
  private _searchTerms: Array<string>;
  private readonly language: string;
  private readonly auth: string;
  private _path: string | undefined;
  private readonly network: AxiosInstance

  get path(): Promise<string> {
    if (this._path === undefined)
      return this.buildPath()
        .then(path => this._path = path);
    return Promise.resolve(this._path);
  }

  get searchTerms(): Array<string> {
    return this._searchTerms;
  }

  set searchTerms(value) {
    this._searchTerms = value;
    this._path = undefined;
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
    this.network = axios.create({
      baseURL: 'https://api.newsriver.io/v2/',
      headers: {
        'Authorization': this.auth,
        'Content-Type': 'application/json'
      },
      withCredentials: true,
      responseType: 'json',
      httpsAgent: new https.Agent({ keepAlive: true }),
      timeout: 500000
    });
    this.doRequest()
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
        const requestData = await this.doRequest();
        await this.updateData(requestData);
      } catch (e) {
        console.error(`Got error ${e} while querying data`);
      }
    }, this.interval);
  }

  async updateData(content: Array<NewsriverData>) {
    try {
      for (const element of content) {
        try {
          const document = await this.db.collection(this.collectionName).doc(element.id).get();
          console.debug(`Item with id ${element.id} ${document.exists ? 'exists' : 'does not exist'}`)
          if (!document.exists)
            firebaseHelper.firestore.createDocumentWithID(this.db, this.collectionName, element.id, element)
              .then(created => console.debug(`Item with ID: ${element.id} was ${created ? 'created' : 'not created'}`))
              .catch(err => console.error(`Error while creating document ${err}`));
          else
            firebaseHelper.firestore.updateDocument(this.db, this.collectionName, element.id, element)
              .then(updated => console.debug(`Item with ID ${element.id} was ${updated ? 'updated' : 'not updated'}`))
              .catch(err => console.error(`Error while updating document ${err}`));
        } catch (err) {
          console.warn(`Error while creating/updating document - ${err}`);
        }
      }
      console.info(`Updated approximately ${content.length} element(s)`);
    } catch (error) {
      console.error(`Unhandled error ${error}`);
    }
  }

  async doRequest(): Promise<Array<NewsriverData>> {
    const response = await this.network.get(await this.path);
    if (response.status === 200)
      return response.data as Array<NewsriverData>;
    else
      throw new TypeError(`The response code is not valid - ${response.status}`);
  }

  async buildPath(): Promise<string> {
    const parts = ['/search?query=']
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
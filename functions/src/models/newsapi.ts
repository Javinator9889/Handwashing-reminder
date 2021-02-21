import { NewsAPIResponse } from '../interfaces/inewsapi';
import { Database } from '../database';
import { ProjectProperties } from '../interfaces/projectProperties';
import properties = require('../common/properties');

class NewsAPIController {
  private readonly databases: Record<string, Database>;
  private readonly cachedResults: Record<string, Array<NewsAPIResponse>>;
  private readonly lastUpdate: Record<string, number>;
  private properties: ProjectProperties;

  constructor() {
    this.databases = {};
    this.cachedResults = {};
    this.lastUpdate = {};
    this.properties =
        properties.projectProperties(properties.firebaseApp, '2.0');
    properties.languages.forEach(language => {
      this.databases[language] = new Database(
          this.properties.database,
          `${this.properties.collection}_${language}`);
      this.lastUpdate[language] = 0;
      this.cachedResults[language] = null;
    });
  }

  async queryNews(language: string): Promise<NewsAPIResponse[]> {
    if (language ! in properties.languages)
      throw new RangeError(`invalid language: "${language}" - ` +
          `available are: ${properties.languages}`);

    if (Math.floor((Date.now() - this.lastUpdate[language] / 60000)) <= 15)
      return this.cachedResults[language];

    const collection = this.databases[language].collection;
    const snapshot = await collection.orderBy('publishedAt', 'desc').get();
    const data = new Array<NewsAPIResponse>();
    snapshot.forEach(item => {
      if (item.data() !== null) data.push(item.data() as NewsAPIResponse);
    });
    this.cachedResults[language] = data;
    this.lastUpdate[language] = Date.now();
    return data;
  }
}


export const newsApiController = new NewsAPIController();

import * as admin from 'firebase-admin';

export class RemoteConfigData {
  remoteConfig: admin.remoteConfig.RemoteConfig;

  constructor(app: admin.app.App) {
    this.remoteConfig = admin.remoteConfig(app);
  }

  getSearchTermsForLanguage(language: string): Promise<Array<string>> {
    return new Promise<Array<string>>(resolve => {
      this.remoteConfig.getTemplate()
        .then(template => {
          let condition: string;
          switch (language) {
            case 'es': condition = 'Spanish users'; break;
            default: condition = 'Default language users'; break;
          }
          const values = JSON.parse(template.parameters['search_terms'].conditionalValues[condition]['value']);
          resolve(values);
        });
    });
  }
}

import {Updater} from './updater';
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions'

export class RemoteConfigData {
  remoteConfig: admin.remoteConfig.RemoteConfig;
  updaters: Record<string, Updater>;

  constructor(app: admin.app.App) {
    this.remoteConfig = admin.remoteConfig(app);
    this.updaters = {};
    this.listenToRCChanges();
  }

  getSearchTermsForLanguage(language: string): Promise<Array<string>> {
    return new Promise<Array<string>>(resolve => {
      this.remoteConfig.getTemplate()
        .then(template => {
          let condition: string;
          switch (language) {
            case 'es':
              condition = 'Spanish users';
              break;
            default:
              condition = 'Default language users';
              break;
          }
          const values = JSON.parse(template.parameters['search_terms'].conditionalValues[condition]['value']);
          resolve(values);
        });
    });
  }

  subscribeUpdaters(updaters: Record<string, Updater>) {
    this.updaters = updaters;
  }

  listenToRCChanges() {
    functions.remoteConfig.onUpdate(_ => {
      return admin.credential.applicationDefault().getAccessToken()
        .then(_ => {
          this.remoteConfig.getTemplate()
            .then(template => {
              const languages = Object.keys(template.parameters['search_terms'].conditionalValues);
              for (const language of languages) {
                const terms = JSON.parse(
                  template.parameters['search_terms'].conditionalValues[language]['value']
                );
                try {
                  if (this.updaters[language].searchTerms.length !== terms.lenght)
                    this.updaters[language].searchTerms = terms;
                } catch (e) {
                  console.warn(`Updaters are not set yet - ${e}`);
                }
              }
            });
        })
        .catch(err => console.error(`Error while obtaining data from RC: ${err}`));
    });
  }
}

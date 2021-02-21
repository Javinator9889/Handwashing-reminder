import {NewsriverUpdater} from './newsriverUpdater';
import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions'

export class RemoteConfigData {
  remoteConfig: admin.remoteConfig.RemoteConfig;
  updaters: Record<string, NewsriverUpdater>;

  constructor(app: admin.app.App) {
    this.remoteConfig = admin.remoteConfig(app);
    this.updaters = {};
    this.listenToRCChanges();
  }

  async getSearchTermsForLanguage(language: string) {
    console.info('Getting template...');
    const template = await this.remoteConfig.getTemplate();
    console.debug('Checking condition');
    let condition: string;
    switch (language) {
      case 'es':
        condition = 'Spanish users';
        break;
      default:
        condition = 'Default language users';
        break;
    }
    console.debug('Parsing JSON');
    return JSON.parse(template.parameters['search_terms'].conditionalValues[condition]['value']);
  }

  subscribeUpdaters(updaters: Record<string, NewsriverUpdater>) {
    this.updaters = updaters;
  }

  listenToRCChanges() {
    console.info('Listening to changes in RemoteConfig...');
    functions.remoteConfig.onUpdate((version, _) => {
      console.debug(`RemoteConfig values have changed - version: ${version}`);
      return admin.credential.applicationDefault().getAccessToken()
        // tslint:disable-next-line:no-shadowed-variable
        .then(_ => {
          this.remoteConfig.getTemplate()
            .then(template => {
              const languages = Object.keys(template.parameters['search_terms'].conditionalValues);
              for (const language of languages) {
                const terms = JSON.parse(
                  template.parameters['search_terms'].conditionalValues[language]['value']
                );
                try {
                  console.debug("Updating updater search terms");
                  if (this.updaters[language].searchTerms.length !== terms.lenght)
                    this.updaters[language].searchTerms = terms;
                } catch (e) {
                  console.warn(`Updaters are not set yet - ${e}`);
                }
              }
            })
            .catch(err => console.warn(`Error while obtaining the template - ${err}`));
        })
        .catch(err => console.error(`Error while obtaining data from RC: ${err}`));
    });
  }
}

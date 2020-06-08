import * as admin from 'firebase-admin';
// import controller = require("../controllers/api");
import {Router} from "express";
// import get = Reflect.get;

/*const router = Router();

router.get('/api/v1', (req, res, next) => {
  try {
    const language = req.query.lang;
    const tokenId = req.get('Authorization').split('Bearer')[0];
    admin.auth().verifyIdToken(tokenId)
      .then(_ => {
        if (language === undefined)
          res.status(403).send('lang must be given [?lang=...]');
        else next();
      })
      .catch(e => {
        console.log('User unauthorized');
        next();
        // res.status(401).send(e)
      })
  } catch (e) {
    console.error(e);
    res.status(401).send(e);
  }
});
router.get('/api/v1', controller.getNewsByLanguage);*/
// app: admin.app.App, apiController: { getNewsByLanguage: (req, res) => any }
export function ApiRoutes(apiController: { getNewsByLanguage: (req, res) => any }) {
  const router = Router();
  router.get('/api/v1', (req, res, next) => {
    try {
      const language = req.query.lang;
      const tokenId = req.get('Authorization').split('Bearer')[0];
      admin.auth().verifyIdToken(tokenId)
        .then(_ => {
          if (language === undefined)
            res.status(403).send('lang must be given [?lang=...]');
          else next();
        })
        .catch(e => {
          console.log('User unauthorized');
          next();
          // res.status(401).send(e)
        })
    } catch (e) {
      console.error(e);
      res.status(401).send(e);
    }
  });
  router.get('/api/v1', apiController.getNewsByLanguage);

  return {router};
}

/*export class ApiRouter {
  router: Router
  // firebaseApp: admin.app.App

  constructor(app: admin.app.App, apiController: ApiController) {
    // this.firebaseApp = app;
    this.router = Router();

    this.router.get('/api/v1', (req, res, next) => {
      try {
        const language = req.query.lang;
        const tokenId = req.get('Authorization').split('Bearer')[0];
        admin.auth(app).verifyIdToken(tokenId)
          .then(_ => {
            if (language === undefined)
              res.status(403).send('lang must be given [?lang=...]');
            else next();
          })
          .catch(e => {
            console.log('User unauthorized');
            next();
            // res.status(401).send(e)
          })
      } catch (e) {
        console.error(e);
        res.status(401).send(e);
      }
    });
    // this.router.get('/api/v1', apiController.getNewsByLanguage);
  }
}*/

// export = router;

/*export class ApiRouter {
  router: Router;
  firebaseApp: admin.app.App;

  constructor(firebaseApp: admin.app.App, apiController: ApiController) {
    this.router = Router();
    this.firebaseApp = firebaseApp;
    // this.router.use('/api/v1', apiController.getNewsByLanguage);
    this.router.use('/update', (req: Request, res: Response) => {
      try {
        apiController.api.updaters['en'].request()
          .then(apiData => {
            apiController.api.updaters['en'].updateData(apiData)
              .then(_ => {
                console.log("Data updated");
                res.json(apiData);
              })
              .catch(err => {
                console.error(err);
                res.sendStatus(500);
              })
          })
          .catch(e => {
            console.error(e);
            res.sendStatus(500);
          });
      } catch (e) {
        console.error(e);
        res.sendStatus(500);
      }
    });
  }

  async init(): Promise<Router> {
    this.router.use('/api/v1', (req: Request, res: Response, next: NextFunction) => {
      try {
        const language = req.query.lang;
        const tokenId = req.get('Authorization').split('Bearer')[1];
        admin.auth(this.firebaseApp).verifyIdToken(tokenId)
          .then(_ => {
            if (language === undefined)
              res.status(403).send('lang must be given [?lang=...]');
            else next();
          })
          .catch(err => res.status(401).send(err));
      } catch (e) {
        res.status(401).send(e);
      }
    });
    return this.router;
  }
}*/

// export const router = express.Router();

/*router.use('/api/v1', (req, res, next) => {
  try {
    const language = req.query.lang;
    const tokenId = req.get('Authorization').split('Bearer ')[1];
    admin.auth().verifyIdToken(tokenId)
      .then(_ => {
        if (language === undefined) {
          res.status(403).send('lang must be given [?lang=...]');
        } else next();
      })
      .catch(err => res.status(401).send(err));
  } catch (e) {
    res.status(401).send(e);
  }
});*/
// router.use('/api/v1', getNewsByLanguage);
// router.use('/update', (req, res) => {
//   api.updaters['es'].request()
//     .then(apiData => {
//       api.updaters['es'].updateData(apiData)
//         .then(() => {
//           console.log("Data was updated");
//           res.json(apiData);
//         })
//         .catch((err) => {
//           console.error(err);
//           res.status(304).send(err);
//         });
//     })
//     .catch((err) => console.error(err));
// });

// module.exports = router;

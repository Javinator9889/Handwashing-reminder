import apiController = require("../controllers/newsriver");
import express = require("express");
// import admin = require("firebase-admin");


const router = express.Router();

apiController.apiModel.initialize()
  .then(_ => apiController.apiModel.scheduleUpdates())
  .catch(e => {
      console.error(e);
      throw e;
    });

router.get('/api/v1', (req, res, next) => {
  try {
    const language = req.query.lang;
    // const tokenId = req.get('Authorization').split('Bearer')[0];
    if (language === undefined)
      res.status(403).send('lang must be given [?lang=...]');
    else next();
    /*admin.auth(apiController.apiModel.firebaseApp).verifyIdToken(tokenId)
      .then(_ => {
        if (language === undefined)
          res.status(403).send('lang must be given [?lang=...]');
        else next();
      })
      .catch(e => {
        console.error('Unauthorized');
        next();
        // res.status(401).send(e);
      });*/
  } catch (e) {
    console.error(`Possible missing authorization header: ${e}`);
    res.status(401).send(e);
  }
});
router.get('/api/v1', apiController.queryNewsForLanguage);
router.get('/close', (req, res) => {
  const authToken = req.get('Authorization');
  if (authToken === undefined)
    return res.sendStatus(403);
  if (authToken === process.env.ADMIN_TOKEN) {
    return apiController.apiModel.stopScheduling()
      .then(_ => res.sendStatus(200))
      .catch(_ => res.sendStatus(200));
  } else
    return res.sendStatus(403);
});

export = router;

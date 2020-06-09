import apiController = require("../controllers/newsriver");
import express = require("express");
import admin = require("firebase-admin");
import properties = require('../common/properties');


const router = express.Router();

apiController.apiModel.initialize()
  .catch(e => {
      console.error(e);
      throw e;
    });

router.get('/api/v1', (req, res, next) => {
  try {
    const language = req.query.lang;
    const tokenId = req.headers.authorization.split(' ')[1];
    admin.auth(properties.firebaseApp).verifyIdToken(tokenId)
      .then(_ => {
        if (language === undefined)
          res.status(403).send('lang must be given [?lang=...]');
        else next();
      })
      .catch(e => {
        console.error('Unauthorized');
        res.status(401).send(e);
      });
  } catch (e) {
    console.error(`Possible missing authorization header: ${e}`);
    res.status(401).send(e);
  }
});
router.get('/api/v1', apiController.queryNewsForLanguage);

export = router;

import * as express from 'express';
import * as admin from 'firebase-admin';
import * as apiController from '../controllers/api';

// export const router = express.Router();
const router = express.Router();

router.use('/api/v1', (req, res, next) => {
  try {
    const tokenId = req.get('Authorization').split('Bearer ')[1];
    admin.auth().verifyIdToken(tokenId)
      .then(_ => {
        const language = req.params.lang;
        if (language === undefined) {
          res.status(403).send('lang must be given [?lang=...]');
        } else next()
      })
    .catch(err => res.status(401).send(err));
  } catch (e) {
    res.status(401).send(e);
  }
});
router.use('/api/v1', apiController.getNewsByLanguage);

module.exports = router;

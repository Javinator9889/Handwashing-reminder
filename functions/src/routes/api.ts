import * as express from 'express';
import * as admin from 'firebase-admin';
import * as apiController from '../controllers/api';

// export const router = express.Router();
const router = express.Router();

router.use('/api/v1', (req, res, next) => {
  const tokenId = req.get('Authorization').split('Bearer ')[1];
  admin.auth().verifyIdToken(tokenId)
    .then(_ => next())
    .catch(err => res.status(401).send(err));
});
router.use('/api/v1', apiController.getNewsByLanguage);

module.exports = router;

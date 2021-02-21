import { Router } from 'express';
import { queryNews } from '../controllers/newsapi';
import { checkRequest } from '../models/tokenauth';

const router = Router();
router.get('/api/v2', async (req, res, next) => {
  try {
    const authOk = await checkRequest(req);
    if (authOk) next();
  } catch (err) {
    console.error(err);
    res.status(401).send(err);
  }
});

router.get('/api/v2', queryNews);

export = router

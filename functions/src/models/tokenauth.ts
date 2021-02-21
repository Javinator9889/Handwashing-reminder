import { Request } from 'express';
import admin = require('firebase-admin');
import properties = require('../common/properties');


export async function checkRequest(req: Request): Promise<boolean> {
  return new Promise<boolean>((resolve, reject) => {
    const language = req.query.lang;
    if (language === undefined || language === null)
      reject(new TypeError('lang query param is missing! [?lang=...]'));

    const bearerToken = req.headers.authorization;
    if (bearerToken === undefined || bearerToken === null)
      reject(new TypeError('Authorization token is missing!'));

    if (!bearerToken.startsWith('Bearer'))
      reject(new SyntaxError(
          'Authorization token is not valid - missing "Bearer" token'));

    const token = bearerToken.slice(7);
    admin.auth(properties.firebaseApp).
        verifyIdToken(token).
        then(_ => resolve(true)).
        catch(e => reject(`Unauthorized: ${e}`));
  });
}

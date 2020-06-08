// import {Api} from '../models/api';
// import api = require('../models/api');
// import {api} from '../models/api';
// import apiModels = require("../models/api");

import {Request, Response} from 'express';
import {Api} from "../models/api";

/*export function getNewsByLanguage(req: Request, res: Response) {
  console.log('Getting news by language');
  const language = req.query.lang as string;
  apiModels.api.newsForLanguage(language)
    .then(data => res.json(data))
    .catch(err => {
      console.error(err);
      res.sendStatus(304);
    });
}*/

export function ApiController(api: Api) {
  return {
    getNewsByLanguage(req: Request, res: Response) {
      const language = req.query.lang as string;
      api.newsForLanguage(language)
        .then(data => res.json(data))
        .catch(err => {
          console.error(err);
          res.sendStatus(500);
        });
    }
  }
}

/*export class ApiController {
  api: Api

  constructor(api: Api) {
    this.api = api;
  }

  getNewsByLanguage(req: Request, res: Response) {
    const language = req.query.lang as string;
    this.api.newsForLanguage(language)
      .then(data => res.json(data))
      .catch(err => {
        console.error(err);
        res.sendStatus(500);
      });
  }
}*/

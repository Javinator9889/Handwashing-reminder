export import apiModel = require("../models/newsriver");
import {Request, Response} from "express";


export async function queryNewsForLanguage(req: Request, res: Response) {
  const language = req.query.lang as string;
  apiModel.newsForLanguage(language)
    .then(newsData => res.json(newsData))
    .catch(err => {
      if (err !instanceof RangeError) {
        console.error(`Error while getting news data: ${err}`);
        res.sendStatus(500);
      }
      else
        res.status(403).send(err);
    })
}
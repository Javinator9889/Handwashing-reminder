export import apiModel = require("../models/newsriver");
import {Request, Response} from "express";


export async function queryNewsForLanguage(req: Request, res: Response) {
  const language = req.query.lang as string;
  const fromParam = req.query.from;
  const amountParam = req.query.amount;
  try {
    const newsData = await apiModel.newsForLanguage(language);
    if (fromParam === undefined && amountParam === undefined) {
      res.json(newsData);
    } else {
      const from = fromParam !== undefined ? Number(fromParam) : 0;
      const amount = amountParam !== undefined ? Number(amountParam) : newsData.length;

      res.json(newsData.slice(from, from + amount));
    }
  } catch (err) {
    if (err !instanceof RangeError) {
      console.error(`Error while getting news data: ${err}`);
      res.sendStatus(500);
    }
    else
      res.status(403).send(err);
  }
}
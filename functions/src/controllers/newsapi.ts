import { Request, Response } from 'express';
import { newsAPIModel } from '../models/newsapi';


export async function queryNews(req: Request, res: Response): Promise<Response> {
  const language = req.query.lang as string;
  const fromParam = req.query.form;
  const amountParam = req.query.amount;
  try {
    const newsData = await newsAPIModel.queryNews(language);
    if (fromParam === undefined && amountParam === undefined)
      return res.json(newsData);
    else {
      const from = fromParam !== undefined ? Number(fromParam) : 0;
      const amount = amountParam !== undefined
          ? Number(amountParam)
          : newsData.length;

      return res.json(newsData.slice(from, from + amount));
    }
  } catch (err) {
    if (err !instanceof RangeError) {
      console.error(`Error while getting news data: ${err}`);
      return res.sendStatus(500);
    } else
      return res.status(403).send(err);
  }
}

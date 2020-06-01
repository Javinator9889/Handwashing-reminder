import {api} from '../models/api';

export async function getNewsByLanguage(req, res) {
  const language = req.query.lang;
  const data = await api.newsForLanguage(language);
  return res.json(data);
}
export interface NewsAPIResponse {
  status: string,
  totalResults: number,
  articles: Array<Article>
}

interface Article {
  source: Source,
  author: string,
  title: string,
  description: string,
  url: string,
  urlToImage: string,
  publishedAt: Date,
  content: string
}

interface Source {
  id: string,
  name: string
}

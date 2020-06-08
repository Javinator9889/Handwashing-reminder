interface NewsriverElements {
    type: string,
    primary: boolean,
    url: string,
    width: number | null,
    height: number | null,
    title: string | null,
    alternative: string | null
  }
  
  interface NewsriverWebsite {
    name: string,
    hostName: string,
    domainName: string,
    iconURL: string,
    countryName: string | null,
    countryCode: string | null,
    region: null
  }
  
  interface Sentiment {
    type: string,
    sentiment: number
  }
  
  interface ReadTime {
    type: string,
    seconds: number
  }
  
  interface NewsriverMetadata {
    finSentiment: Sentiment,
    readTime: ReadTime
  }
  
  export interface NewsriverData {
    id: string,
    discoverDate: Date,
    title: string,
    language: string,
    text: string,
    structuredText: string,
    elements: Array<NewsriverElements>,
    website: NewsriverWebsite,
    metadata: NewsriverMetadata,
    highlight: string,
    score: number
  }
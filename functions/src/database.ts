class Database {
  private readonly db: FirebaseFirestore.Firestore;
  private readonly collectionName: string;

  get collection(): FirebaseFirestore.CollectionReference {
    return this.db.collection(this.collectionName);
  }

  constructor(db: FirebaseFirestore.Firestore, collectionName: string) {
    this.db = db;
    this.collectionName = collectionName;
  }
}
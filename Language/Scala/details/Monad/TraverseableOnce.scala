  implicit class MonadOps[+A](trav: TraversableOnce[A]) {
    def map[B](f: A => B): TraversableOnce[B] = trav.toIterator map f
    def flatMap[B](f: A => GenTraversableOnce[B]): TraversableOnce[B] = trav.toIterator flatMap f
    def withFilter(p: A => Boolean) = trav.toIterator filter p
    def filter(p: A => Boolean): TraversableOnce[A] = withFilter(p)
  }
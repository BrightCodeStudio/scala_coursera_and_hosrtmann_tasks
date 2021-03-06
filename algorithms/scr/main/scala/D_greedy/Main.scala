package D_greedy

import scala.collection.mutable

/**
  * По данной непустой строке
  * s длины не более 10&#94;4,
  * состоящей из строчных букв латинского алфавита,
  * постройте оптимальный беспрефиксный код. В первой строке выведите количество различных букв k, встречающихся в строке,
  * и размер получившейся закодированной строки.
  * В следующих k строках запишите коды букв в формате "letter: code".
  * В последней строке выведите закодированную строку.
  */
object Main {
  case class Edge(code: Option[String] = None, prev: Option[Edge] = None) {

    override def toString: String = s"edge: Edge($code,$prev)"
  }
  trait Tree[T] {
    def priority: Int
    def edge:     Option[Edge]
    def updateEdges(): Tree[T] = this match {
      case l: Leaf[T] =>
        l.copy(edge = edge)
      case n: Node[T] =>
        n.copy(edge = edge)
          .copy(left = n.left.updateEdges(), right = n.right.updateEdges())
    }
    def setCode(c: String): Tree[T] = this match {
      case l: Leaf[T] =>
        l.copy(
          edge = l.edge.map(_.copy(code = Some(c))).orElse(Some(Edge(Some(c))))
        )
      case n: Node[T] =>
        n.copy(
          edge = n.edge.map(_.copy(code = Some(c))).orElse(Some(Edge(Some(c))))
        )
    }

    def asString(acc: String = ""): String = this match {
      case Leaf(value, edge, priority) =>
        acc + s"Leaf(value: $value, edge: $edge, priority: $priority)"
      case Node(left, right, edge, priority) =>
        val next = acc + "     "
        acc + s"""Node(
         ${left.asString(next)}
         ${right.asString(next)}
         ${acc + edge}
         ${acc + s"priority: $priority"}
         $acc)
         """
    }

    override def toString: String = this.asString()
  }
  object Tree {
    def create[T](value: (T, Int)): Tree[T] =
      Leaf(value._1, Some(Edge(Some("0"))), value._2)
    def create[T](value1: (T, Int), value2: (T, Int)): Tree[T] = {
      val ((t1, p1), (t2, p2)) =
        if (value1._2 <= value2._2) (value1, value2) else (value2, value1)
      Node(
        Leaf(t1, Some(Edge(Some("0"))), p1),
        Leaf(t2, Some(Edge(Some("1"))), p2),
        None,
        p1 + p2
      )
    }
    def add[T](v1: T, p1: Int, tree: Tree[T]): Tree[T] =
      if (p1 <= tree.priority) {
        val t = tree.setCode("1")
        Node(
          Leaf(v1, Some(Edge(Some("0"), t.edge)), p1),
          t,
          None,
          p1 + tree.priority
        ).updateEdges()
      } else {
        val t = tree.setCode("0")
        Node(
          t,
          Leaf(v1, Some(Edge(Some("1"), t.edge)), p1),
          None,
          p1 + tree.priority
        ).updateEdges()
      }

    def merge[T](tree1: Tree[T], tree2: Tree[T]): Tree[T] = {
      val (t1, t2) =
        if (tree1.priority <= tree2.priority) tree1 -> tree2 else tree2 -> tree1

      Node(
        t1.setCode("0"),
        t2.setCode("1"),
        None,
        t1.priority + t2.priority
      ).updateEdges()
    }
  }
  case class Node[T](
    left:     Tree[T],
    right:    Tree[T],
    edge:     Option[Edge],
    priority: Int
  ) extends Tree[T]

  case class Leaf[T](value: T, edge: Option[Edge], priority: Int)
      extends Tree[T]

  type E     = Either[Char, Tree[Char]]
  type A     = (E, Int)
  type Queue = mutable.PriorityQueue[A]
  implicit val ord: Ordering[A] = (x: A, y: A) =>
    -Ordering.Int.compare(x._2, y._2)
  def frequency(chars: Array[Char]): Map[E, Int] = {
    val (m, (c, freq)) =
      chars.sorted.foldLeft((Map.empty[E, Int], ('a', 0))) {
        case ((m, (accChar, freq)), c) =>
          accChar match {
            case a if a == c =>
              (m, (accChar, freq + 1))
            case aa if aa != c =>
              val mm = if (freq == 0) m else m.+((Left(accChar), freq))
              (mm, (c, 1))
          }
      }
    m.+((Left(c), freq))
  }

  def code(in: String): Unit = {
    println(in)
    val chars = in.toCharArray
    val map   = frequency(chars)
    println(map)
    val ppq = mutable.PriorityQueue.empty[A]
    ppq.addAll(map)
    println(ppq)
    def recur(pq: mutable.PriorityQueue[A]): mutable.PriorityQueue[A] =
      if (pq.size >= 2) {
        val (e1, i1) = pq.dequeue()
        println(s"ppq get next = ($e1,$i1)")
        val (e2, i2) = pq.dequeue()
        println(s"ppq get next = ($e2,$i2)")
        (e1, e2) match {
          case (c1: Left[Char, Tree[Char]], c2: Left[Char, Tree[Char]]) =>
            val leaf = Tree.create((c1.value, i1), (c2.value, i2))
            pq.enqueue((Right(leaf), leaf.priority))
            recur(pq)
          case (c1: Left[Char, Tree[Char]], t2: Right[Char, Tree[Char]]) =>
            val node = Tree.add(c1.value, i1, t2.value)
            pq.enqueue((Right(node), node.priority))
            recur(pq)
          case (t1: Right[Char, Tree[Char]], c2: Left[Char, Tree[Char]]) =>
            val node = Tree.add(c2.value, i2, t1.value)
            pq.enqueue((Right(node), node.priority))
            recur(pq)
          case (t1: Right[Char, Tree[Char]], t2: Right[Char, Tree[Char]]) =>
            val node = Tree.merge(t1.value, t2.value)
            pq.enqueue((Right(node), node.priority))
            recur(pq)
        }
      } else {
        println(s"nothing do ${pq}")
        pq
      }
    val tree: Tree[Char] =
      recur(ppq).dequeue()._1.getOrElse(sys.error("queue is empty"))

    println(tree)
  }
  def main(args: Array[String]): Unit =
//    val in = scala.io.StdIn.readLine()
//    code(in)

//    val tests = List(
//      "a" -> List(1 -> 1, List('a' -> "0"), "0"),
//      "abacabad" -> List(
//        4 -> 14,
//        List('a' -> "0", 'b' -> "10", 'c' -> "110", 'd' -> "111"),
//        "01001100100111"
//      ),
//      "accepted" -> List(
//        6 -> 20,
//        List(
//          'p' -> "110",
//          'a' -> "111",
//          'c' -> "10",
//          't' -> "011",
//          'd' -> "010",
//          'e' -> "00"
//        ),
//        "11110100011001100010"
//      )
//    )

//    code(tests.head._1)
//    code(tests.tail.head._1)
//    code(tests.tail.tail.head._1)
    code("beep boop beer!")

}

package io.getquill.examples

import io.getquill._

object SimpleExamples {

  val ctx = new SqlMirrorContext(PostgresDialect, SnakeCase)
  import ctx._

  case class Person(id:Int, name:String, age:Int)
  case class Address(ownerId:Int, street:String, zip:Int, state:String)

  val peopleAndAddresses = quote {
    for {
      p <- query[Person]
      a <- query[Address]
             if (p.id == a.ownerId)
    } yield (p, a)
  }

  val peopleAndAddressesExplicit = quote {
    for {
      p <- query[Person]
      a <- query[Address].leftJoin(a =>
             p.id == a.ownerId
           )
    } yield (p, a)
  }



  val joes = quote {
    query[Person].filter(p => p.name == "Joe")
  }
  val jims = quote {
    query[Person].filter(p => p.name == "Jim")
  }
  val joesAndJims = quote {
    joes ++ jims
  }

  def main(args:Array[String]) = {
    //println(run(peopleAndAddresses).string)
    //println(run(peopleAndAddressesExplicit).string)

    println(run(joesAndJims.map(r => (r.name, r.age))).string)
  }

}

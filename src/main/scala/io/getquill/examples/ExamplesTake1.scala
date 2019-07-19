package io.getquill.examples

import io.getquill._

object ExamplesTake1 {

  val ctx = new SqlMirrorContext(PostgresDialect, Literal)
  import ctx._

  val merchantClients = quote {
    for {
      mc <- query[MerchantClient]
      r  <- query[Registry]
        .join(r =>
          r.alias == mc.alias &&
            r.market == "us" &&
            r.recordType == "M")
    } yield Client(mc.alias, mc.uid, mc.code, mc.accountTag)
  }

  val serviceClients = quote {
    joinPartnershipCode(
      filterByRegistry[ServiceClient](
        query[ServiceClient], "us", "S"
      )
    ).map { case (sc, code) =>
      Client(sc.alias, 999, code, sc.accountTag)
    }
  }

  val joinPartnershipCode = quote {
    (serviceClients: Query[ServiceClient]) =>
      for {
        sc <- serviceClients
        p <- query[Partnership]
          .join(p => p.id == sc.partnershipFk)
      } yield (sc, p.code)
  }

  def filterByRegistry[T <: {def alias:String}] = quote {
    (clients: Query[T], market:String, recordType:String) =>
      for {
        c <- clients
        r  <- query[Registry]
          .join(r =>
            r.alias == c.alias &&
              r.market == market &&
              r.recordType == recordType)
      } yield c
  }

  val clients = quote {
    merchantClients ++ serviceClients
  }

  def registryExample() = {
    case class Foo(id:Int, bar:Long, alias:String)

    val q = quote {
      filterByRegistry[Foo](query[Foo], "GuessWho", "R")
    }

    println(run(q).string)
  }

  def joinPartnershipExample() = {
    val filteredClients = quote {
      query[ServiceClient]
    }

    val joinedWithPartnership = quote {
      joinPartnershipCode(filteredClients)
    }

    println(run(joinedWithPartnership).string)
  }

  def `serviceClients...Almost`() = quote {
    joinPartnershipCode(
      filterByRegistry[ServiceClient](
        query[ServiceClient], "us", "M"
      )
    )
  }

  def main(args:Array[String]):Unit = {
    //registryExample()
    //joinPartnershipExample()

    println(run(clients).string)

  }

}

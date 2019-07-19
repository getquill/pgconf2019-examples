package io.getquill.examples

import io.getquill._

object ExamplesNewRequirements {

  val ctx = new SqlMirrorContext(PostgresDialect, SnakeCase)
  import ctx._

  val partneredServiceClients = quote {
    (market: String) => {
      joinPartnership(
        filterByRegistry[ServiceClient](
          query[ServiceClient], market, "S")
      )
    }
  }

  val usServiceClients = quote {
    partneredServiceClients("us").map { case (sc, partnership) =>
      Client(sc.alias, 999, partnership.code, sc.accountTag)
    }
  }

  val europeanServiceClients = quote {
    for {
      (sc, p) <- partneredServiceClients("eu")
      pu      <- query[PartnershipUid].join(pu => pu.partnershipFk == p.id)
    } yield Client(
      sc.alias, pu.uid, p.code, sc.accountTag
    )
  }

  val europeanServiceClientsFirstAttempt = quote {
    for {
      (sc, p) <- joinPartnership(
                   filterByRegistry[ServiceClient](
                     query[ServiceClient], "eu", "S"
                   )
                 )
      pu      <- query[PartnershipUid].join(pu => pu.partnershipFk == p.id)
    } yield Client(
      sc.alias, pu.uid, p.code, sc.accountTag
    )
  }

  val joinPartnership = quote {
    (serviceClients: Query[ServiceClient]) =>
      for {
        sc <- serviceClients
        p <- query[Partnership]
          .join(p => p.id == sc.partnershipFk)
      } yield (sc, p)
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

  val merchantClients = quote {
    (market:String) => {
      for {
        mc <- query[MerchantClient]
        r  <- query[Registry]
          .join(r =>
            r.alias == mc.alias &&
              r.market == "us" &&
              r.recordType == "M")
      } yield Client(mc.alias, mc.uid, mc.code, mc.accountTag)
    }
  }

  val usClients = quote {
    merchantClients("us") ++ usServiceClients
  }


}

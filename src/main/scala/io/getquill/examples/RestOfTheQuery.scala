package io.getquill.examples

import io.getquill._
import io.getquill.java.FormatterInEngine

object RestOfTheQuery {

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
        r  <- query[Registry].join(r =>
            r.alias == mc.alias &&
              r.market == "us" &&
              r.recordType == "M")
      } yield Client(mc.alias, mc.uid, mc.code, mc.accountTag)
    }
  }

  val usClients = quote {
    merchantClients("us") ++ usServiceClients
  }

  val europeanClients = quote {
    merchantClients("eu") ++ europeanServiceClients
  }

  val caClients = quote {
    merchantClients("ca")
  }


  val accountMappings = quote {
    for {
      account <- query[Account]
      accountType <- query[AccountType].join(
        accountType => account.`type` == accountType.accountType
      )
      dedicatedAccount <- query[DedicatedAccount].join(
        dedicatedAccount => dedicatedAccount.accountNumber == account.number
      )
    } yield (account, accountType, dedicatedAccount)
  }

  val mapAccountsToClient = quote {
    (clients: Query[Client]) =>
      for {
        client <- clients
        (account, accountType, dedicatedAccount) <- accountMappings.join(am => {
          am match { case (account, accountType, dedicatedAccount) =>
            accountType.mappingType == 0 ||
              (accountType.mappingType == 1 && account.tag == client.accountTag) ||
              (accountType.mappingType == 2 && dedicatedAccount.clientAlias == client.alias)
          }
        })
      } yield (client, account)
  }

  val formatOutput = quote {
    (clientsAndAccounts: Query[(Client, Account)]) => {
      clientsAndAccounts.map{ case (client, account) => (
        account.name,
        client.alias,
        if (client.uid == 999)
          infix"${account.number}".as[String]
        else
          infix"${account.number}".as[String] + infix"substring(${client.alias}, 1, 2)".as[String],
        if (liftQuery(Set("A", "S")).contains(client.code))
          "ST"
        else
          "ENH"
      )}
    }
  }

  def main(args:Array[String]):Unit = {
    val f = new FormatterInEngine()

    val q = quote {
      formatOutput(mapAccountsToClient(usClients))
    }

    //println(run(usClients).string)
    println(f.format(run(q).string))

  }


}

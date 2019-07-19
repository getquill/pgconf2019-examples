package io.getquill.examples

class Model {

}


case class MerchantClient(alias:String, uid:Int, code:String, accountTag:String)
case class ServiceClient(alias:String, accountTag:String, partnershipFk:Int)

case class Client(alias:String, uid:Int, code:String, accountTag:String)

case class Registry(alias:String, recordType:String, market:String)

case class Partnership(id: Int, code: String)

case class PartnershipUid(partnershipFk: Int, uid: Int)


case class Account(name:String, `type`:String, number:Int, tag:String)
case class AccountType(accountType:String, mappingType:Int)
case class DedicatedAccount(accountNumber:Int, clientAlias:String)
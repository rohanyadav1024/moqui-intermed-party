import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityCondition
import org.moqui.entity.EntityFind
import org.moqui.entity.EntityList
import org.moqui.entity.EntityValue

ExecutionContext ec = context.ec

EntityFind ef = ec.entity.find("assignment.assign3.party.FindCustomerView").distinct(true)
ef.selectField("partyId")

if (firstName){
    ef.condition("firstName", firstName)}
if (secondName){
    ef.condition("secondName", secondName)}
if (emailAddress){
    ef.condition("emailAddress", emailAddress)}
if (contactNumber){
    ef.condition("contactNumber", contactNumber)}
if (address1){
    ef.condition(ec.entity.conditionFactory.makeCondition("address1", EntityCondition.LIKE, (leadingWildcard ? "%" : "") + address1 + "%").ignoreCase())
//    ef.condition("address1", address1)
}
if (address2){
    ef.condition(ec.entity.conditionFactory.makeCondition("address1", EntityCondition.LIKE, (leadingWildcard ? "%" : "") + address1 + "%").ignoreCase())
//    ef.condition("address2", address2)
}
if (city){
    ef.condition("city", city)}
if (postalCode){
    ef.condition("postalCode", postalCode)}

if (orderByField) {
    if (orderByField.contains("combinedName")) {
        ef.orderBy("organizationName,firstName,lastName")
    } else {
        ef.orderBy(orderByField)
    }
}

if (!pageNoLimit) { ef.offset(pageIndex as int, pageSize as int); ef.limit(pageSize as int) }

partyIdList = []
EntityList el = ef.list()
for (EntityValue ev in el) partyIdList.add(ev.partyId)

partyIdListCount = ef.count()
partyIdListPageIndex = ef.pageIndex
partyIdListPageSize = ef.pageSize
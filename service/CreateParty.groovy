import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue

ExecutionContext ec = context.ec

// ✅ Step 1: Check if a party with the given email already exists
EntityValue existingParty = ec.entity.find("assignment.assign3.party.contact.ContactMech")
        .condition("infoString", emailAddress)
        .condition("contactMechTypeEnumId", "CmtEmailAddress")
        .useCache(false)
        .one()

if (existingParty) {
    ec.message.addError("A party with email ${emailAddress} already exists.")
    return
}

// ✅ Step 2: Create a New Party (Person) with Role "Customer"
EntityValue party = ec.entity.makeValue("assignment.assign3.party.Party")
party.setSequencedIdPrimary()  // Generates a unique partyId
party.partyTypeEnumId = "PtyPerson"
party.create()
String partyId = party.partyId

// Assign role "Customer" to Party
EntityValue partyRole = ec.entity.makeValue("assignment.assign3.party.PartyRole")
partyRole.partyId = partyId
partyRole.roleTypeId = "Customer"
partyRole.create()

// ✅ Step 3: Create a Person and Link It to Party
EntityValue person = ec.entity.makeValue("assignment.assign3.party.Person")
person.partyId = partyId
person.firstName = firstName
person.lastName = lastName
person.create()

// ✅ Step 4: Create ContactMech (Email - Required)
EntityValue emailContactMech = ec.entity.makeValue("assignment.assign3.party.contact.ContactMech")
emailContactMech.setSequencedIdPrimary()
emailContactMech.contactMechTypeEnumId = "CmtEmailAddress"
emailContactMech.infoString = emailAddress
emailContactMech.create()
String emailContactMechId = emailContactMech.contactMechId

// Link ContactMech to Party
EntityValue partyContactMechEmail = ec.entity.makeValue("assignment.assign3.party.contact.PartyContactMech")
partyContactMechEmail.partyId = partyId
partyContactMechEmail.contactMechId = emailContactMechId
partyContactMechEmail.contactMechPurposeId = "EmailPrimary"
partyContactMechEmail.fromDate = ec.user.nowTimestamp
partyContactMechEmail.create()

// ✅ If Postal Address is Provided, Create ContactMech & PostalAddress
if (address1) {
    EntityValue postalContactMech = ec.entity.makeValue("assignment.assign3.party.contact.ContactMech")
    postalContactMech.setSequencedIdPrimary()
    postalContactMech.contactMechTypeEnumId = "CmtPostalAddress"
    postalContactMech.create()
    String postalContactMechId = postalContactMech.contactMechId

    EntityValue postalAddress = ec.entity.makeValue("assignment.assign3.party.contact.PostalAddress")
    postalAddress.contactMechId = postalContactMechId
    postalAddress.address1 = address1
    postalAddress.address2 = address2
    postalAddress.city = city
    postalAddress.postalCode = postalCode
    postalAddress.create()

    // Link Postal Address to Party
    EntityValue partyContactMechPostal = ec.entity.makeValue("assignment.assign3.party.contact.PartyContactMech")
    partyContactMechPostal.partyId = partyId
    partyContactMechPostal.contactMechId = postalContactMechId
    partyContactMechPostal.contactMechPurposeId = "PostalPrimary"
    partyContactMechPostal.fromDate = ec.user.nowTimestamp
    partyContactMechPostal.create()
    context.postalContactMechId = postalContactMechId
}

// ✅ If Contact Number is Provided, Create ContactMech & TelecomNumber
if (contactNumber) {
    EntityValue telecomContactMech = ec.entity.makeValue("assignment.assign3.party.contact.ContactMech")
    telecomContactMech.setSequencedIdPrimary()
    telecomContactMech.contactMechTypeEnumId = "CmtTelecomNumber"
    telecomContactMech.create()
    String telecomContactMechId = telecomContactMech.contactMechId

    EntityValue telecomNumber = ec.entity.makeValue("assignment.assign3.party.contact.TelecomNumber")
    telecomNumber.contactMechId = telecomContactMechId
    telecomNumber.contactNumber = contactNumber
    telecomNumber.create()

    // Link Telecom Number to Party
    EntityValue partyContactMechTelecom = ec.entity.makeValue("assignment.assign3.party.contact.PartyContactMech")
    partyContactMechTelecom.partyId = partyId
    partyContactMechTelecom.contactMechId = telecomContactMechId
    partyContactMechTelecom.contactMechPurposeId = "PhonePrimary"
    partyContactMechTelecom.fromDate = ec.user.nowTimestamp
    partyContactMechTelecom.create()
    context.telecomContactMechId = telecomContactMechId
}

// ✅ Step 5: Store Values in Output Parameters for CreateCustomer.xml
context.partyId = partyId
context.emailContactMechId = emailContactMechId
//context.postalContactMechId = postalContactMechId ?: null
//context.telecomContactMechId = telecomContactMechId ?: null
import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import java.sql.Timestamp

ExecutionContext ec = context.ec
Timestamp today = ec.user.nowTimestamp

// ✅ Step 2: Check if a party with the given email exists
EntityValue partyContactMech = ec.entity.find("assignment.assign3.party.contact.PartyContactMech")
        .condition("contactMechPurposeId", "EmailPrimary")
        .condition("contactMechId", ec.entity.find("assignment.assign3.party.contact.ContactMech")
                .condition("infoString", emailAddress)
                .condition("contactMechTypeEnumId", "CmtEmailAddress")
                .useCache(false)
                .one()?.contactMechId)
        .useCache(false)
        .one()

if (!partyContactMech) {
    ec.message.addError("No customer found with email ${emailAddress}.")
    return
}

String partyId = partyContactMech.partyId

// ✅ Step 3: Update Person Attributes
EntityValue person = ec.entity.find("assignment.assign3.party.Person")
        .condition("partyId", partyId)
        .one()

if (person) {
    if (firstName) person.firstName = firstName
    if (lastName) person.lastName = lastName
    person.update()
}

// ✅ Step 4: Update Telecom Contact (Create New & Mark Old as Inactive)
if (contactNumber) {
    EntityValue oldTelecomMech = ec.entity.find("assignment.assign3.party.contact.PartyContactMech")
            .condition("partyId", partyId)
            .condition("contactMechPurposeId", "PhonePrimary")
            .one()

    if (oldTelecomMech) {
        // Mark old telecom contact as inactive
        oldTelecomMech.thruDate = today
        oldTelecomMech.update()
    }

    // Create new telecom contact mechanism
    EntityValue telecomContactMech = ec.entity.makeValue("assignment.assign3.party.contact.ContactMech")
    telecomContactMech.setSequencedIdPrimary()
    telecomContactMech.contactMechTypeEnumId = "CmtTelecomNumber"
    telecomContactMech.create()
    String telecomContactMechId = telecomContactMech.contactMechId

    // Create new telecom number entity
    EntityValue telecomNumber = ec.entity.makeValue("assignment.assign3.party.contact.TelecomNumber")
    telecomNumber.contactMechId = telecomContactMechId
    telecomNumber.contactNumber = contactNumber
    telecomNumber.create()

    // Link new telecom contact with the party
    EntityValue newPartyContactMech = ec.entity.makeValue("assignment.assign3.party.contact.PartyContactMech")
    newPartyContactMech.partyId = partyId
    newPartyContactMech.contactMechId = telecomContactMechId
    newPartyContactMech.contactMechPurposeId = "PhonePrimary"
    newPartyContactMech.fromDate = today
    newPartyContactMech.create()

    context.telecomContactMechId = telecomContactMechId
}

// ✅ Step 5: Update Postal Address (Create New & Mark Old as Inactive)
if (address1) {
    EntityValue oldPostalMech = ec.entity.find("assignment.assign3.party.contact.PartyContactMech")
            .condition("partyId", partyId)
            .condition("contactMechPurposeId", "PostalPrimary")
            .one()

    if (oldPostalMech) {
        // Mark old postal contact as inactive
        oldPostalMech.thruDate = today
        oldPostalMech.update()
    }

    // Create new postal contact mechanism
    EntityValue postalContactMech = ec.entity.makeValue("assignment.assign3.party.contact.ContactMech")
    postalContactMech.setSequencedIdPrimary()
    postalContactMech.contactMechTypeEnumId = "CmtPostalAddress"
    postalContactMech.create()
    String postalContactMechId = postalContactMech.contactMechId

    // Create new postal address entity
    EntityValue postalAddress = ec.entity.makeValue("assignment.assign3.party.contact.PostalAddress")
    postalAddress.contactMechId = postalContactMechId
    postalAddress.address1 = address1
    postalAddress.address2 = address2
    postalAddress.city = city
    postalAddress.postalCode = postalCode
    postalAddress.create()

    // Link new postal contact with the party
    EntityValue newPartyContactMech = ec.entity.makeValue("assignment.assign3.party.contact.PartyContactMech")
    newPartyContactMech.partyId = partyId
    newPartyContactMech.contactMechId = postalContactMechId
    newPartyContactMech.contactMechPurposeId = "PostalPrimary"
    newPartyContactMech.fromDate = today
    newPartyContactMech.create()

    context.postalContactMechId = postalContactMechId
}

// ✅ Step 6: Store Updated Values in Output Context
context.partyId = partyId
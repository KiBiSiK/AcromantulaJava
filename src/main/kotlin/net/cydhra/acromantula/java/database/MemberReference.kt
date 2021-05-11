package net.cydhra.acromantula.java.database

import org.jetbrains.exposed.dao.id.IntIdTable

class MemberReference

internal object MemberReferenceTable : IntIdTable("references") {
    val referrer = reference("referrer", JavaMethodTable)
    val referred = reference("referred", JavaIdentifierTable)

    val instructionNumber = integer("instruction")
    val type = enumeration("type", MemberReferenceType::class)
}

enum class MemberReferenceType {
    CLASS, METHOD, FIELD
}
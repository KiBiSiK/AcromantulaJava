package net.cydhra.acromantula.java.mapping.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object JavaIdentifierTable : IntIdTable() {
    val identifier = varchar("identifier", Short.MAX_VALUE.toInt())
}

class JavaIdentifier(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<JavaIdentifier>(JavaIdentifierTable)

    /**
     * String value of the identifier. Do not update without updating the associated java source files as well
     */
    var value by JavaIdentifierTable.identifier
}
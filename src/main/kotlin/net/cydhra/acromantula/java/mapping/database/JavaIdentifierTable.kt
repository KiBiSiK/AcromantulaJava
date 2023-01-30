package net.cydhra.acromantula.java.mapping.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object JavaIdentifierTable : IntIdTable() {
    val identifier = varchar("identifier", Short.MAX_VALUE.toInt())
}

class JavaIdentifier(val identifier: String) {
    lateinit var databaseId: EntityID<Int>
}
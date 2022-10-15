package net.cydhra.acromantula.java.mapping.visitors

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

/**
 * A cache for the [JavaIdentifierTable] inserts and lookups that happen during mapping
 */
object IdentityCache {

    private val loader = object : CacheLoader<String, EntityID<Int>>() {
        override fun load(key: String): EntityID<Int> {
            return transaction {
                JavaIdentifierTable.select { JavaIdentifierTable.identifier eq key }.first()[JavaIdentifierTable.id]
            }
        }
    }

    private val cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build(loader)

    /**
     * Insert a new identity into the database and cache it
     */
    fun insertIdentity(identity: String) {
        transaction {
            cache.put(identity, JavaIdentifierTable.insertIgnoreAndGetId {
                it[identifier] = identity
            }!!)
        }
    }

    fun bulkInsert(identities: List<String>) {
        transaction {
            val surrogates = JavaIdentifierTable.batchInsert(
                identities, ignore = true, shouldReturnGeneratedValues = true
            ) { identity ->
                this[JavaIdentifierTable.identifier] = identity
            }

            identities.zip(surrogates).forEach { (identity, result) ->
                cache.put(identity, result[JavaIdentifierTable.id])
            }
        }
    }

    /**
     * Get the surrogate id for a given identifier and cache it if it isn't already
     */
    fun getIdentity(identity: String): EntityID<Int> {
        return cache.get(identity)
    }
}
package net.cydhra.acromantula.java.mapping.visitors

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import net.cydhra.acromantula.java.mapping.database.JavaIdentifierTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit

/**
 * A cache for the [JavaIdentifierTable] inserts and lookups that happen during mapping
 *
 * todo bulk insert-and-ignore identities per instance of an [IdentityClassVisitor], because they do not need the ids
 *  until the next visitor pass, which will lookup them in the cache.
 */
object IdentityCache {

    val loader = object : CacheLoader<String, EntityID<Int>>() {
        override fun load(key: String): EntityID<Int> {
            return transaction {
                JavaIdentifierTable.select { JavaIdentifierTable.identifier eq key }.first()[JavaIdentifierTable.id]
            }
        }
    }

    val cache = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.SECONDS)
        .build(loader)

    /**
     * Insert a new identity into the database and cache it
     */
    fun insertIdentity(identity: String): EntityID<Int> {
        transaction {
            cache.put(identity, JavaIdentifierTable.insertIgnoreAndGetId {
                it[identifier] = identity
            }!!)
        }

        return cache[identity]
    }

    /**
     * Get the surrogate id for a given identifier and cache it if it isn't already
     */
    fun getIdentity(identity: String): EntityID<Int> {
        return cache.get(identity)
    }
}
/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity

import llh.fanclubvup.apiserver.utils.IdGenerator
import org.babyfish.jimmer.sql.meta.UserIdGenerator

class EntityIdGenerator : UserIdGenerator<Long> {
    override fun generate(entityType: Class<*>?): Long {
        return IdGenerator.nextId()
    }
}

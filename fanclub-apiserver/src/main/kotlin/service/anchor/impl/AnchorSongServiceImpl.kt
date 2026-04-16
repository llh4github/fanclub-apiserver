/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor.impl

import llh.fanclubvup.apiserver.entity.anchor.AnchorSong
import llh.fanclubvup.apiserver.entity.anchor.bid
import llh.fanclubvup.apiserver.entity.anchor.id
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.anchor.AnchorSongService
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.springframework.stereotype.Service

@Service
class AnchorSongServiceImpl(
    sqlClient: KSqlClient,
) : AnchorSongService,
    BaseDatabaseServiceImpl<AnchorSong>(AnchorSong::class, sqlClient) {

    override fun deleteByIds(ids: List<Long>, bid: BID?): Int {
        return sqlClient.transaction {
            createDelete {
                where(table.id.valueIn(ids))
                bid?.let {
                    where(table.bid.eq(it))
                }
            }.execute()
        }
    }

}

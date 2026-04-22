/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.dto.DeleteIds
import llh.fanclubvup.apiserver.entity.anchor.AnchorSong
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import llh.fanclubvup.common.BID
import kotlin.reflect.KProperty1

interface AnchorSongService : BaseDatabaseService<AnchorSong> {

    companion object {

        val defaultUniqueKeys: List<KProperty1<AnchorSong, *>> =
            listOf(AnchorSong::bid, AnchorSong::name)
    }

    fun deleteByIds(ids: List<Long>, bid: BID?): Int

    fun deleteByIds(idsWrapper: DeleteIds, bid: BID?): Int {
        return deleteByIds(idsWrapper.ids, bid)
    }
}

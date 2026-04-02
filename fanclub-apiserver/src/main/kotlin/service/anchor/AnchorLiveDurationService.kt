/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.anchor

import llh.fanclubvup.apiserver.entity.anchor.AnchorLiveDuration
import llh.fanclubvup.apiserver.service.BaseDatabaseService
import java.time.LocalDate

interface AnchorLiveDurationService : BaseDatabaseService<AnchorLiveDuration> {

    fun computeLiveDuration(roomId: Long, date: LocalDate): Int
}

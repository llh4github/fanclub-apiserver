/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

import com.github.yitter.contract.IdGeneratorOptions
import com.github.yitter.idgen.YitIdHelper

object IdGenerator {

    init {
        YitIdHelper.setIdGenerator(IdGeneratorOptions(1))
    }

    fun nextId(): Long = YitIdHelper.nextId()

    fun nextIdStr(): String = nextId().toString()
}

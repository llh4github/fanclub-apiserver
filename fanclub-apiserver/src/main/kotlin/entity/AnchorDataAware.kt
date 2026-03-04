/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity

import org.babyfish.jimmer.sql.MappedSuperclass

/**
 * TODO 要根据 bili 开放接口内容再设计
 */
@MappedSuperclass
interface AnchorDataAware {

    val anchorId: Long
}

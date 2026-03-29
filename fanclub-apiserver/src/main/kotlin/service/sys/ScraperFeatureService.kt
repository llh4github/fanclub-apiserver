/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service.sys

import llh.fanclubvup.apiserver.entity.sys.ScraperFeature
import llh.fanclubvup.apiserver.entity.sys.dto.ScraperFeatureFollowerEnabledView
import llh.fanclubvup.apiserver.service.BaseDatabaseService

interface ScraperFeatureService : BaseDatabaseService<ScraperFeature> {

    fun queryFollowerEnabled():List<ScraperFeatureFollowerEnabledView>
}

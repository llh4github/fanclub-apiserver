package llh.fanclubvup.apiserver.service.sys.impl

import llh.fanclubvup.apiserver.entity.sys.ScraperFeature
import llh.fanclubvup.apiserver.service.BaseDatabaseServiceImpl
import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Service

@Service
class ScraperFeatureServiceImpl(
    sqlClient: KSqlClient,
) : ScraperFeatureService,
    BaseDatabaseServiceImpl<ScraperFeature>(ScraperFeature::class, sqlClient)
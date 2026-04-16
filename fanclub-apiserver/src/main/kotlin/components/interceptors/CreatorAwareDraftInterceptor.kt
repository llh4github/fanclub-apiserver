package llh.fanclubvup.apiserver.components.interceptors

import llh.fanclubvup.apiserver.entity.CreatorAware
import llh.fanclubvup.apiserver.entity.CreatorAwareDraft
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.springframework.stereotype.Component

@Component
class CreatorAwareDraftInterceptor : DraftInterceptor<CreatorAware, CreatorAwareDraft> {
    override fun beforeSave(
        draft: CreatorAwareDraft,
        original: CreatorAware?
    ) {
        if (!isLoaded(draft, CreatorAware::updatedBy)) {
            draft.updatedBy {
                id = SecurityContextUtil.currentUserId()
            }
        }

        if (original == null) {
            if (!isLoaded(draft, CreatorAware::createdBy)) {
                draft.createdBy {
                    id = SecurityContextUtil.currentUserId()
                }
            }
        }
    }
}


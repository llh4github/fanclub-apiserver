package llh.fanclubvup.apiserver.exception

import org.babyfish.jimmer.error.ErrorFamily

@ErrorFamily
enum class AnchorOperateErrorCode {

    /**
     * 缺少主播信息
     *
     * 已后台帐号，但未绑定主播信息
     */
    ABSENCE_ANCHOR_INFO,

    /**
     * BID不存在
     */
    BID_NOT_EXIST,

    /**
     * 房间ID不存在
     */
    ROOM_ID_NOT_EXIST,
}

/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.enums

/**
 * 舰长等级枚举类
 */
@Deprecated("移动到sdk模块里")
enum class FanLevel(val level: Int, val displayName: String) {
    /** 总督 */
    ADMIRAL(1, "总督"),

    /** 提督 */
    VICE_ADMIRAL(2, "提督"),

    /** 舰长 */
    CAPTAIN(3, "舰长");

    companion object {
        /**
         * 根据等级数字获取对应的枚举值
         * @param level 等级数字(1-3)
         * @return 对应的FanLevel枚举值，如果找不到则返回null
         */
        fun fromLevel(level: Int): FanLevel? {
            return entries.find { it.level == level }
        }

        /**
         * 根据显示名称获取对应的枚举值
         * @param displayName 显示名称
         * @return 对应的FanLevel枚举值，如果找不到则返回null
         */
        fun fromDisplayName(displayName: String): FanLevel? {
            return entries.find { it.displayName == displayName }
        }
    }
}
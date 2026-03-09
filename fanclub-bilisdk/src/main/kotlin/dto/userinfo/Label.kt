/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.bilisdk.dto.userinfo

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 会员标签信息
 * 示例数据：
 * {
 *   "path": "http://i0.hdslb.com/bfs/vip/label_annual.png",
 *   "text": "年度大会员",
 *   "label_theme": "annual_vip",
 *   "text_color": "#FFFFFF",
 *   "bg_style": 1,
 *   "bg_color": "#FB7299",
 *   "use_img_label": true,
 *   "label_id": -22
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Label(
    /**
     * 标签路径，示例：http://i0.hdslb.com/bfs/vip/label_annual.png
     */
    @JsonProperty("path")
    val path: String? = null,
    /**
     * 标签文本，示例：年度大会员
     */
    @JsonProperty("text")
    val text: String? = null,
    /**
     * 标签主题，示例：annual_vip
     */
    @JsonProperty("label_theme")
    val labelTheme: String? = null,
    /**
     * 文本颜色（十六进制），示例：#FFFFFF
     */
    @JsonProperty("text_color")
    val textColor: String? = null,
    /**
     * 背景样式，示例：1
     */
    @JsonProperty("bg_style")
    val bgStyle: Int? = null,
    /**
     * 背景颜色（十六进制），示例：#FB7299
     */
    @JsonProperty("bg_color")
    val bgColor: String? = null,
    /**
     * 边框颜色，示例：
     */
    @JsonProperty("border_color")
    val borderColor: String? = null,
    /**
     * 是否使用图片标签，示例：true
     */
    @JsonProperty("use_img_label")
    val useImgLabel: Boolean? = null,
    /**
     * 简体中文图片标签URI，示例：
     */
    @JsonProperty("img_label_uri_hans")
    val imgLabelUriHans: String? = null,
    /**
     * 繁体中文图片标签URI，示例：
     */
    @JsonProperty("img_label_uri_hant")
    val imgLabelUriHant: String? = null,
    /**
     * 简体中文静态图片标签URI，示例：https://i0.hdslb.com/bfs/vip/8d4f8bfc713826a5412a0a27eaaac4d6b9ede1d9.png
     */
    @JsonProperty("img_label_uri_hans_static")
    val imgLabelUriHansStatic: String? = null,
    /**
     * 繁体中文静态图片标签URI，示例：https://i0.hdslb.com/bfs/activity-plat/static/20220614/e369244d0b14644f5e1a06431e22a4d5/VEW8fCC0hg.png
     */
    @JsonProperty("img_label_uri_hant_static")
    val imgLabelUriHantStatic: String? = null,
    /**
     * 标签ID，示例：-22
     */
    @JsonProperty("label_id")
    val labelId: Int? = null,
    /**
     * 标签跳转链接
     */
    @JsonProperty("label_goto")
    val labelGoto: LabelGoto? = null
)
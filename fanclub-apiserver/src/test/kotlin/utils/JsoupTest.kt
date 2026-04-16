/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.utils

import org.jsoup.Jsoup
import kotlin.test.Test


class JsoupTest {

    @Test
    fun testJsoup(){
        val url = "https://www.bilibili.com/opus/1159880489834643463"
        
        // 设置请求头模拟浏览器访问，避免被B站反爬拦截
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeout(10000)
            .get()
        
        // 提取所有img标签的src属性（转换为绝对URL）
        val imageUrls = doc.select("img")
            .map { it.absUrl("src") }
            .filter { url ->
                // 过滤条件：
                // 1. 包含 bfs/new_dyn/ （B站动态图片特征）
                // 2. 包含 @ 符号（带图片处理参数，如 @264w_264h_1e_1c）
                // 3. 包含 .png （PNG格式，注意.png后面可能还有处理参数）
                url.contains("bfs/new_dyn/") && 
                url.contains("@") && 
                url.contains(".png")
            }
            .distinct() // 去重
        
        // 输出结果
        println("找到 ${imageUrls.size} 张符合条件的图片：")
        imageUrls.forEachIndexed { index, imageUrl ->
            println("${index + 1}. $imageUrl")
        }
    }
}
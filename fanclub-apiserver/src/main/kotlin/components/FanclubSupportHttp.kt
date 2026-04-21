package llh.fanclubvup.apiserver.components

import llh.fanclubvup.apiserver.components.properties.FanclubSupportProperty
import llh.fanclubvup.apiserver.dto.DanmuReq
import llh.fanclubvup.apiserver.dto.StartLiveReq
import llh.fanclubvup.apiserver.dto.StopLiveReq
import llh.fanclubvup.apiserver.utils.ThirdPartyHttpClient
import org.springframework.stereotype.Component

@Component
class FanclubSupportHttp(
    property: FanclubSupportProperty
) {
    private val client by lazy {
        ThirdPartyHttpClient(property.baseUrl)
    }

    fun startLive(req: StartLiveReq) {
        client.executePost("/live/start", req, Unit::class.java)
    }

    fun stopLive(req: StopLiveReq) {
        client.executePost("/live/stop", req, Unit::class.java)
    }

    fun danmu(req: DanmuReq) {
        client.executePost("/danmu/receive", req, Unit::class.java)
    }
}

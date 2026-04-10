package llh.fanclubvup.apiserver.statistics.handlers

import llh.fanclubvup.bilibili.dm.DanmuCommandHandler
import llh.fanclubvup.bilibili.dm.cmd.DanmuMsgCommand
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class DanmuMsgCommandHandler : DanmuCommandHandler<DanmuMsgCommand> {
    override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
        TODO("Not yet implemented")
    }

    override fun supportedCommand(): KClass<DanmuMsgCommand> = DanmuMsgCommand::class
}

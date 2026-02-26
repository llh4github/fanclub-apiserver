package llh.fanclubvup.dm.cmd

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "cmd"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = SendGiftCommand::class, name = "SEND_GIFT"),
    JsonSubTypes.Type(value = DanmakuCommand::class, name = "DANMU_MSG")
)
abstract class Command {

    abstract val cmd: String
}

package llh.fanclubvup.dm.cmd

// 6. 扩展函数增强功能
val Command.isSendGift: Boolean get() = this is SendGiftCommand
val Command.isDanmaku: Boolean get() = this is DanmakuCommand
val Command.isUnknown: Boolean get() = this is UnknownCommand

// 弹幕命令的扩展
val DanmakuCommand.content: String get() = danmakuInfo.content
val DanmakuCommand.sender: String get() = userInfo.username

// 礼物命令的扩展
val SendGiftCommand.totalValue: Int get() = num * price
val SendGiftCommand.formattedSender: String get() = "$username($uid)"
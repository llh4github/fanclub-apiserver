---
name: 生成建表创建弹幕数据处理器
description: 创建用于处理弹幕websocket接收到数据的处理器
---

1. 用户应指定一个弹幕数据具体类型，是位于`llh.fanclubvup.bilibili.dm.cmd`包下的类，并且应当实现接口`llh.fanclubvup.bilibili.dm.cmd.Command`。
2. 新建的处理器类应当是SpringBean。使用`@Component`注解修饰。
3. 新建的处理器类应当实现 `DanmuCommandHandler` 接口。下面是示例：
    ```kotlin
    @Component
   class DanmuMsgCommandHandler : DanmuCommandHandler<DanmuMsgCommand>, BaseMsgCommandHandler() {
       private val logger = KotlinLogging.logger {}
       override fun handle(cmd: DanmuMsgCommand, roomId: Long) {
       }

       override fun supportedCommand(): KClass<DanmuMsgCommand> = DanmuMsgCommand::class
   }
   ```
4. 新建的处理器类应当在`DanmuHandlerGather`注册。先从构造器中注册，然后添加到list集合中去。
    ```kotlin
    @Component
    class DanmuHandlerGather(
       danmuMsgCommandHandler: DanmuMsgCommandHandler,
    ) {
        val handlers = listOf<DanmuCommandHandler<*>>(
            danmuMsgCommandHandler,
        )
    }
   ```

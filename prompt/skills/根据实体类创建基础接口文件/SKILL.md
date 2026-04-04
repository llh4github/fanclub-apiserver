---
name: 根据实体类创建基础接口文件
description: 根据Jimmer实体类创建对应的service类，controller类，dto文件。
---

# service层类创建规则

1. 不要在service包下直接创建类。 参考`entity.anchor.AnchorInfo`类，对应的service类为`service.anchor.AnchorInfoService`
2. 实现类放在其impl包内。
3. service层的接口类继承BaseDatabaseService接口，实现类额外继承BaseDatabaseServiceImpl类。
4. service实现类需要注入 sqlClient: KSqlClient 对象。 基本形式为：
   ```kotlin
   @Service
   class AnchorFollowerNumServiceImpl(
    sqlClient: KSqlClient,
   ) : AnchorFollowerNumService,
   BaseDatabaseServiceImpl<AnchorFollowerNum>(AnchorFollowerNum::class, sqlClient){
   }
   ```

# dto文件创建规则

1. dto类放在src/main/dto目录内。
2. dto文件名格式为：`dto/entity_package_name/entity_name.dto`,参考`entity.anchor.AnchorInfo`类，对应的dto文件为`dto/anchor/AnchorInfo.dto`
3. dto文件使用文档： https://babyfish-ct.github.io/jimmer-doc/zh/docs/object/view/dto-language?_highlight=dto 。必须包含文件头格式为：
   ```
   export llh.fanclubvup.apiserver.entity.anchor.AnchorInfo
    -> package llh.fanclubvup.apiserver.entity.anchor.dto
   ```
4. 实体类对应的dto文件内只创建两个类(只需变更类名、注释即可)，例如：
    ```
    @io.swagger.v3.oas.annotations.media.Schema(title = "数据分页查询请求体")
    specification AnchorInfoQuerySpec {
        pageParam: llh.fanclubvup.apiserver.dto.PageQueryRequest
    }
   @io.swagger.v3.oas.annotations.media.Schema(title = "分页查询结果数据")
   AnchorInfoPageView {
       #allScalars
   }
   ```
5. dto文件中定义的类使用KSP编译生成。

# controller层类的创建规则

1. controller类放在api包内，类名以Api为后缀。
2. 类存放位置示例：对于`entity.anchor.AnchorInfo`类, 对应的controller类为`api.anchor.AnchorInfoApi`
3. 类包含必要的注解，默认注入对应的service类。例如：
   ```kotlin
   @RestController
   @RequestMapping("/anchor/info")
   @Tag(name = "主播信息接口")
   class AnchorInfoApi(
   private val service: AnchorInfoService
   ) {
   }
   ```
4. 生成一个分页查询接口，这个接口的参数为第二步中生成的dto文件中的类。例如：
   ```kotlin
    @PostMapping("/page")
    @Operation(summary = "分页查询")
    fun pageQuery(@RequestBody queryParam: AnchorInfoQuerySpec) =
        JsonWrapper.ok(
            service.pageQuery(AnchorInfoPageView::class, queryParam, queryParam.pageParam)
        )
   ```
   

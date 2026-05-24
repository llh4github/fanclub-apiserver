# 全局规则

1. 用中文回答所有问题
2. 使用 go 1.26.2 语法 , 特别注意new关键字的使用
3. 函数、方法必须有简明的注释
4. 结构体与文件名不要重复包名。即不要 `UserService` 这样的形式，而是在 `services` 包下直接创建 `User` 结构体
5. 使用 <https://goproxy.cn> 作为 go mod 的代理
6. 结构体字段的 json 标签采用snake_case形式
7. 复杂的函数与方法使用中文，并且遵循注释规范，示例如下：
   ```go
   // LTTB downsamples time series data using the Largest Triangle Three Buckets algorithm.
   //
   // Parameters:
   //   - data: ordered list of points sorted by X (timestamp)
   //   - threshold: target number of points to retain (minimum 2)
   //
   // Returns:
   //   - downsampled slice of Points
   //
   // Panics if threshold < 2 or data is empty.
   func LTTB(data []Point, threshold int) []Point {}
   ```
8. 使用 `g/logger.go` 文件方法记录日志。
9. 数据库表模型的主键及关联字段必须是 int64 类型，使用 `json:"id,string"` 标签表示。
10. 函数名、字段名、结构体名没有充足理由时，不允许导出。
11. 对于 int64 类型的字段，json 序列化时必须使用 `json:"field,string"` 标签表示。

# ORM 使用规则

1. ORM使用的是gorm 库， 使用文档为：https://gorm.io/docs
2. 在仅查询操作中: 使用`errors.Is(err, gorm.ErrRecordNotFound)`判断是否为数据不存在错误，如果是则返回 nil或零值。
3. 修改`database/model`目录下的文件后，需要运行代码生成任务，参考`.vscode/tasks.json`文件中 gorm-gen 任务。

## 类型安全的查询条件

**类型安全的关联查条件构建示例**：

```go
	q := typed.G[model.SysUser](g.DB)
        q.
		Select(
			generated.BaseModel.ID,
			generated.SysUser.Username,
			generated.SysUser.Password,
			generated.SysUser.Role,
			generated.AnchorInfo.Bid,
			generated.AnchorInfo.RoomID,
		).
		Where(generated.SysUser.Username.Eq(loginReq.Username)).
		Joins(clause.LeftJoin.Association("Anchor"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Scan(appCtx.C, &user)
```

**类型安全的排序条件构建示例**：

```go
		 typed.G[model.AnchorLiveSchedule](g.DB).
			Select(
				generated.AnchorLiveSchedule.Topic,
				generated.AnchorLiveSchedule.Emoji,
				generated.AnchorLiveSchedule.StartTime,
				generated.AnchorLiveSchedule.EndTime,
			).
			Where(generated.AnchorLiveSchedule.Bid.Eq(bid)).
			Where(generated.AnchorLiveSchedule.StartTime.Gte(startOfWeek)).
			Where(generated.AnchorLiveSchedule.StartTime.Lte(endOfWeek)).
			Order(clause.OrderBy{
				Columns: []clause.OrderByColumn{
					generated.AnchorLiveSchedule.StartTime.Asc(),
				},
			})
```

# Rest 接口编写规则

## 请求参数

请求参数必须在 `req` 包中定义为结构体。
请求参数结构体字段必须有 json swagger validate 标签。
新增或修改了请求路径后，需检查请求与swagger注释是否一致。
结构体和它的字段都必须简明的中文注释。如：

```go
// CreateAnchorFollowerNum 创建主播粉丝数记录请求
type CreateAnchorFollowerNum struct {
	// 粉丝数
	FollowerNum int `json:"follower_num" example:"123456" validate:"required,min=0"`
	// 统计日期
	CntDate string `json:"cnt_date" example:"2026-04-25" validate:"required,datetime=2006-01-02"`
	// B站UID
	Bid int64 `json:"bid" example:"12345678" validate:"required,min=1"`
}
```

请求参数需在请求方法中进行校验，使用 `rest/base.go` 中定义的常量：

```go
const errMsgInvalidParams = "请求参数不正确" // query 参数错误
const errMsgInvalidBody = "请求体不正确"   // body 参数错误

var req req.CreateAnchorFollowerNum
if err := ctx.Bind().Body(&req); err != nil {
    return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
}

// 验证请求参数
if err := validate.Struct(req); err != nil {
    return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
}
```

## 响应返回值

所有接口只能返回 200 HTTP 响应码。
错误响应时，必须使用 errors 包下定义的 ErrorCode 错误码。

## HTTP API 类编写规则

对于需要认证的接口，必须在swagger注释中添加 `@Security BearerAuth`。
HTTP API 接口文件都放在 rest 包下。
结构体名为当前实体类名但不导出。如 `anchorFollowerNum` 结构体放在 `anchor_follower_num.go` 文件中：

```go
// anchorFollowerNum 主播粉丝数控制器
type anchorFollowerNum struct {
}
```

router 方法需当前文件中注册，然后在 `rest/base.go` 文件中调用方法。如：

```go
func rigsterAnchorFollowerNum(router fiber.Router) {
    apis := new(anchorFollowerNum)
    router.Group("anchor/followerNum").
        Post("/add", apis.Create).
        Put("/update", apis.Update).
        Get("/list", apis.List).
        Get("/get", apis.GetByBiliIDAndDate).
        Delete("/delete", apis.Delete)
}
```

router 方法必须有 swagger 注释。
对于分页接口的 router 方法，正常响应的 swagger 注释里使用 `JsonPageResp` 结构体。

swagger 文档中的 `@Param` 注释应使用请求结构体，而不是重复声明每个字段：

```go
// 正确：使用结构体
// @Param req query req.GetAnchorSong true "查询参数"

// 错误：重复声明字段
// @Param bid query int true "B站UID"
// @Param name query string true "歌曲名称"
```

```go
// @Success 200 {object} wrapper.JsonPageResp[ent.AnchorFollowerNum]
```

# Services 层代码编写规则

所有服务层代码都放在 services 包下。
服务层代码文件名与结构体名保持一致。如 `anchor_follower_num.go` 文件中定义 `AnchorFollowerNum` 结构体。
定义上无字段的结构体。此结构体不导出。如：

```go
type anchorFollowerNumService struct {
}
```

导出结构体实例，如：`var AnchorFollowerNum = new(anchorFollowerNumService)`

# 项目本地启动

参考 `.vscode/launch.json` 文件。使用 vscode 的调试功能启动项目。

# 测试文件编写规则

所有涉及到数据库与外部资源的测试都必须放在 `integration` 包下。

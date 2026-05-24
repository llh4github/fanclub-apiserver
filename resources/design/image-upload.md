# 图片上传处理流程文档

本文档描述项目中图片上传处理的完整流程与逻辑，供前端和后端开发参考。

## 一、架构概述

```
前端 → 后端API（生成凭证） → 前端直传OSS → 后端API（保存内容）
           ↓
       七牛云存储
```

### 核心组件

| 组件       | 文件路径                                                                                                                     | 说明            |
| -------- | ------------------------------------------------------------------------------------------------------------------------ | ------------- |
| REST API | [api/rest/oss.go](file:///c:/person-data/golang/fanclub-go/api/rest/oss.go)                                              | 图片上传凭证接口      |
| 存储服务     | [storage/oss.go](file:///c:/person-data/golang/fanclub-go/storage/oss.go)                                                | 七牛云存储封装       |
| 数据模型     | [database/model/treehole\_submission.go](file:///c:/person-data/golang/fanclub-go/database/model/treehole_submission.go) | 图片链接存储        |
| 内容处理     | [utils/markdown.go](file:///c:/person-data/golang/fanclub-go/utils/markdown.go)                                          | Markdown 图片提取 |

***

## 二、上传凭证接口

### 接口信息

```
GET /oss/image/upload?filename=xxx.jpg
```

### 请求参数

| 参数       | 类型     | 必填 | 说明        |
| -------- | ------ | -- | --------- |
| filename | string | 是  | 文件名（不含路径） |

### 响应参数

```json
{
  "token": "xxx...",
  "object_key": "uploads/2024/05/abc123.jpg",
  "region": "local",
  "expires_at": 1716547200
}
```

| 参数          | 类型     | 说明             |
| ----------- | ------ | -------------- |
| token       | string | 七牛上传凭证         |
| object\_key | string | 对象存储路径         |
| region      | string | 存储区域           |
| expires\_at | int64  | 过期时间（Unix 时间戳） |

### 限流配置

- **限制**: 5次/分钟/IP
- **中间件**: `middleware.OSSRateLimiter`

### Token 使用限制

当前设计采用 **Scope Token（限定路径上传凭证）**，即：

- **一个 token 只能上传一张图片**
- 每次上传前都需要先请求后端 API 获取新的 token
- token 只能上传到返回的 `object_key` 指定路径

这种设计的优点：

1. **更安全**：token 无法上传到其他路径，防止滥用
2. **路径可控**：后端可以精确控制图片的存储路径
3. **便于审计**：每张图片的上传都有对应的 token 请求记录

***

## 三、上传流程详解

### 1. 客户端请求凭证

```javascript
// 前端调用示例
const response = await fetch('/oss/image/upload?filename=photo.jpg');
const { token, object_key, region, expires_at } = await response.json();
```

### 2. 后端生成凭证

**代码位置**: [api/rest/oss.go](file:///c:/person-data/golang/fanclub-go/api/rest/oss.go)

```go
// GenerateImageUploadCredential 生成图片上传凭证
func (o *oss) GenerateImageUploadCredential(ctx fiber.Ctx) error {
    // 1. 解析请求参数
    var req req.GenerateImageUploadCredential
    if err := ctx.Bind().Query(&req); err != nil {
        return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
    }

    // 2. 验证文件名
    if err := validate.Struct(req); err != nil {
        return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
    }

    // 3. 生成上传凭证
    uploadToken, err := storage.GenerateUploadToken(ctx.Context(), storage.PresignedPutObjectInput{
        Key:         "uploads/" + time.Now().Format("2006/01/02/") + utils.GetUUID(),
        ContentType: "image/*",
        ExpiresIn:   5 * time.Minute,
        MaxSize:     10 * 1024 * 1024, // 10MB
    })
    if err != nil {
        return errs.WrapError(err, "生成上传凭证失败", string(errs.InternalError))
    }

    // 4. 返回凭证
    return wrapper.Success(ctx, &resp.ImageUploadCredential{
        Token:     uploadToken.Token,
        ObjectKey: uploadToken.ObjectKey,
        Region:    uploadToken.Region,
        ExpiresAt: uploadToken.ExpiresAt,
    })
}
```

### 3. 前端直传七牛云

```javascript
// 使用七牛 JS SDK 上传
import * as qiniu from 'qiniu-js';

const observable = qiniu.upload(file, objectKey, token, {
    fname: filename,
    params: {},
    config: {
        useCdnDomain: true,
        region: qiniu.region.z2
    }
});

observable.subscribe({
    next: (res) => { /* 上传进度 */ },
    error: (err) => { /* 上传失败 */ },
    complete: (res) => { /* 上传完成，获取图片URL */ }
});
```

### 4. 存储路径规则

```
uploads/{年}/{月}/{日}/{UUID}.{扩展名}

示例:
uploads/2024/05/19/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg
```

***

## 四、存储配置

### config.toml 配置

```toml
[oss]
endpoint = "localhost:7070"      # 七牛云兼容服务地址
accessKeyID = "admin"           # Access Key
accessKeySecret = "123456"      # Secret Key
bucket = "fanclub"              # 存储桶名称
region = "local"                # 区域标识
useSSL = false                  # 是否使用 HTTPS
```

### 存储服务方法

**代码位置**: [storage/oss.go](file:///c:/person-data/golang/fanclub-go/storage/oss.go)

| 方法                          | 说明           | 参数                                   |
| --------------------------- | ------------ | ------------------------------------ |
| `InitOSS()`                 | 初始化七牛云客户端    | 无                                    |
| `GenerateUploadToken()`     | 生成上传凭证       | Key, ContentType, ExpiresIn, MaxSize |
| `GeneratePresignedGetURL()` | 生成私有资源访问 URL | Key, ExpiresIn, UseCDN               |
| `PutObject()`               | 服务端直传对象      | Key, Body, ContentType               |
| `DeleteObject()`            | 删除对象         | Key                                  |
| `DeleteObjects()`           | 批量删除对象       | Keys                                 |
| `GetObjectInfo()`           | 获取对象信息       | Key                                  |
| `ObjectExists()`            | 检查对象是否存在     | Key                                  |
| `ListObjects()`             | 列举对象         | Prefix, MaxKeys                      |

***

## 五、图片在 Markdown 中的处理

### 处理流程

```
用户输入 Markdown → Goldmark 解析 → Bluemonday 安全过滤 → 提取图片链接
```

### 关键常量

**代码位置**: [utils/markdown.go](file:///c:/person-data/golang/fanclub-go/utils/markdown.go)

```go
const (
    MaxContentLength = 800  // 最大内容字数
    SummaryLength = 100    // 摘要长度
    MaxImageCount = 5      // 最大允许的图片数量
)
```

### 图片链接过滤规则

只有 `*.likofan` 域名的图片会被保留，其他域名图片会被过滤移除：

```markdown
![描述](https://img.likofan.com/image.jpg)  → 保留 ✓
![描述](https://other.com/image.jpg)        → 移除 ✗
```

### 处理结果结构

```go
// MarkdownResult Markdown 处理结果
type MarkdownResult struct {
    HTMLContent string   // HTML 格式的内容
    PlainText   string   // 纯文本内容
    Summary     string   // 摘要（前100字）
    ImageURLs   []string // likofan 域名下的图片链接列表
}
```

***

## 六、数据存储

### TreeholeSubmission 模型

**代码位置**: [database/model/treehole\_submission.go](file:///c:/person-data/golang/fanclub-go/database/model/treehole_submission.go)

```go
type TreeholeSubmission struct {
    BaseModel
    SubmissionID       string         // 稿件ID（36进制编码）
    TopicID            int64          // 关联的主题ID
    ContentMarkdown    string         // 原始投稿内容 (Markdown格式)
    ContentHtml        string         // 投稿内容 (HTML格式)
    Summary            string         // 投稿摘要
    SubmitTime         time.Time      // 投稿时间
    AuditStatus        int            // 审核状态: 0=不宜展示, 1=未审核, 2=可以展示
    ImageURLs          pq.StringArray // 图片链接列表
    ImageURLsConfirmed bool          // 图片链接是否已与 OSS 确认
}
```

***

## 七、前端集成示例

### 1. 单张图片上传

由于采用 Scope Token 设计，**每上传一张图片需要请求一次 token**：

```javascript
import * as qiniu from 'qiniu-js';

// 上传单张图片到 OSS
async function uploadImage(file) {
    // 1. 获取上传凭证（每张图片都要请求一次）
    const { token, object_key } = await fetch(
        `/oss/image/upload?filename=${encodeURIComponent(file.name)}`
    ).then(r => r.json());

    // 2. 上传到七牛云
    const observable = qiniu.upload(file, object_key, token, {
        fname: file.name,
        params: {},
        config: { useCdnDomain: true, region: qiniu.region.z2 }
    });

    return new Promise((resolve, reject) => {
        observable.subscribe({
            next: (res) => console.log(`上传进度: ${res.total.percent}%`),
            error: (err) => reject(err),
            complete: (res) => resolve(object_key)
        });
    });
}

// 插入 Markdown 图片语法
function insertMarkdownImage(file, objectKey) {
    const markdown = `![${file.name}](https://img.likofan.com/${objectKey})`;
    // 插入到编辑器
    editor.insertContent(markdown);
}
```

### 2. 多张图片批量上传

```javascript
// 批量上传多张图片
async function uploadImages(files) {
    const results = [];
    
    for (const file of files) {
        try {
            const objectKey = await uploadImage(file);
            results.push({
                name: file.name,
                key: objectKey,
                url: `https://img.likofan.com/${objectKey}`
            });
        } catch (err) {
            console.error(`上传 ${file.name} 失败:`, err);
        }
    }
    
    return results;
}

// 使用示例：上传用户选择的图片
const fileInput = document.getElementById('image-input');
fileInput.addEventListener('change', async (e) => {
    const files = Array.from(e.target.files);
    const uploaded = await uploadImages(files);
    
    // 将图片插入 Markdown 编辑器
    uploaded.forEach(img => {
        editor.insertContent(`\n![${img.name}](${img.url})\n`);
    });
});
```

### 3. Markdown 编辑器图片预览

```javascript
// 点击图片时触发大图预览
document.querySelectorAll('.markdown-content img').forEach(img => {
    if (img.dataset.previewSrc) {
        img.style.cursor = 'zoom-in';
        img.addEventListener('click', () => {
            // 使用 viewer.js 预览
            const viewer = new Viewer(img);
            viewer.show();
        });
    }
});
```

***

## 八、安全考虑

### 1. 图片来源限制

- 仅允许 `*.likofan` 域名的图片
- 外部图片链接会被自动移除

### 2. 上传限制

| 限制项     | 值        | 说明                         |
| ------- | -------- | -------------------------- |
| 单文件大小   | 10MB     | 通过 `MaxSize` 参数限制          |
| 每篇投稿图片数 | 5张       | 通过 `MaxImageCount` 常量限制    |
| 内容字数    | 800字     | 通过 `MaxContentLength` 常量限制 |
| 上传凭证有效期 | 5分钟      | 通过 `ExpiresIn` 参数限制        |
| 接口限流    | 5次/分钟/IP | 通过中间件限制                    |

### 3. 审核状态

| 值 | 状态   | 说明         |
| - | ---- | ---------- |
| 0 | 不宜展示 | 图片或内容不符合规范 |
| 1 | 未审核  | 等待审核       |
| 2 | 可以展示 | 审核通过       |

***

## 九、错误处理

### 常见错误码

| 错误码                       | 说明       | 处理建议         |
| ------------------------- | -------- | ------------ |
| `OSS_UPLOAD_TOKEN_FAILED` | 生成上传凭证失败 | 检查 OSS 配置或重试 |
| `IMAGE_TOO_MANY`          | 图片数量超过限制 | 提示用户移除部分图片   |
| `CONTENT_TOO_LONG`        | 内容字数超限   | 提示用户精简内容     |
| `RATE_LIMIT_EXCEEDED`     | 请求频率超限   | 提示用户稍后重试     |

### 错误响应示例

```json
{
    "code": "IMAGE_TOO_MANY",
    "msg": "图片数量不能超过5张"
}
```

***

## 十、调试接口

### 本地测试上传凭证

```bash
curl -X GET "http://localhost:8080/oss/image/upload?filename=test.jpg"
```

### 响应示例

```json
{
    "code": "0",
    "data": {
        "token": "xxx...",
        "object_key": "uploads/2024/05/19/abc123.jpg",
        "region": "local",
        "expires_at": 1716547200
    }
}
```


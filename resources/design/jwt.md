# JWT 双 Token 认证方案

双 Token 滑动窗口（Sliding Session with Rotation）是目前业界对抗 Token 窃取（Theft）和 重放攻击（Replay）的最佳实践。

其核心逻辑是：每次刷新 Access Token 时，不仅下发新的 Access Token，同时强制下发一个新的 Refresh Token，并使旧 Refresh Token 立即失效。

## 实现方案

### Token 类型

| Token 类型      | 用途   | 过期时间    | 存储位置           |
| ------------- | ---- | --------- | -------------- |
| Access Token  | 接口认证 | 2 小时（可配置） | 内存/Pinia       |
| Refresh Token | 刷新令牌 | 4 小时（可配置） | HttpOnly Cookie |

### Cookie 配置

| 属性        | 值                        | 说明                 |
| ----------- | ------------------------- | ------------------ |
| Name        | `refresh_token`           | Cookie 名称          |
| HttpOnly    | `true`                   | 禁止 JavaScript 访问  |
| Secure      | 生产环境 `true`，开发环境 `false` | 仅 HTTPS 传输         |
| SameSite    | `Strict`                 | 防止 CSRF 攻击        |
| Path        | `/`                      | 整站生效              |
| Domain      | 可选，跨子域时需设置        | 用于跨子域共享 Cookie   |

### 核心流程

#### 1. 登录流程

```
用户登录 → 验证用户名密码 → 生成 Access Token + Refresh Token → 存入 Redis
→ 返回 Token 对（JSON） + 设置 Refresh Token Cookie（Set-Cookie）
```

#### 2. Token 刷新流程（滑动窗口）

```
请求携带 Cookie（自动包含 refresh_token）→ 验证 Refresh Token 签名
→ 检查 Redis 中是否存在 → 使旧 Token 失效 → 生成新的 Token 对
→ 返回新 Access Token（JSON） + 更新 Refresh Token Cookie（Set-Cookie）
```

#### 3. 接口认证流程

```
请求头携带 Access Token → 验证 Token 签名 → 检查 Redis 中是否存在 → 通过则放行
```

#### 4. 登出流程

```
用户登出 → 删除 Redis 中该用户所有 Token Key → 清除 Refresh Token Cookie
→ 前端清除本地 Access Token
```

### API 接口

| 接口     | 方法   | 路径           | 需要认证 | 说明                          |
| ------ | ---- | -------------- | ---- | --------------------------- |
| 登录    | POST | /auth/login    | 否   | 验证用户名密码，返回 Token 对        |
| 刷新令牌 | POST | /auth/refresh  | 否   | 从 Cookie 获取 Refresh Token    |
| 登出    | POST | /auth/logout   | 是   | 使当前用户所有 Token 失效          |

### 接口请求响应示例

#### 登录 /auth/login

**请求：**

```json
{
  "username": "admin",
  "password": "encrypted_password",
  "session_id": "session_xxx",
  "captcha_token": "captcha_xxx"
}
```

**响应：**

```
HTTP/1.1 200 OK
Set-Cookie: refresh_token=eyJhbGciOiJIUzI1NiIs...; Path=/; HttpOnly; SameSite=Strict; Secure

{
  "code": 0,
  "msg": "success",
  "data": {
    "id": "1",
    "username": "admin",
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiration_time": "2026-05-14T17:30:00Z"
  }
}
```

> **说明**：`refresh_token` 不再通过 JSON 返回，已迁移至 HttpOnly Cookie。

#### 刷新令牌 /auth/refresh

**请求（自动携带 Cookie）：**

```
POST /auth/refresh
Cookie: refresh_token=eyJhbGciOiJIUzI1NiIs...
```

**响应：**

```
HTTP/1.1 200 OK
Set-Cookie: refresh_token=新token; Path=/; HttpOnly; SameSite=Strict; Secure

{
  "code": 0,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "access_token": "新access_token...",
    "expiration_time": "2026-05-14T19:30:00Z"
  }
}
```

#### 登出 /auth/logout

**请求头：**

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Cookie: refresh_token=eyJhbGciOiJIUzI1NiIs...
```

**响应：**

```
HTTP/1.1 200 OK
Set-Cookie: refresh_token=; Path=/; HttpOnly; Max-Age=0

{
  "code": 0,
  "msg": "success",
  "data": "登出成功"
}
```

### Claims 结构

```go
type Claims struct {
    Type   string  // "access" 或 "refresh"
    UserID int64   // 用户ID
    Role   string  // 用户角色
    BID    int64   // B站UID
    RoomID int64   // 房间ID
    jwt.RegisteredClaims
}
```

### 错误处理

| 场景        | Error Code         | 错误信息          | 处理方式      |
| --------- | ------------------ | ------------- | --------- |
| 缺少 Token  | `AuthFailed`       | "缺少认证凭证"      | 返回 401     |
| Token 已过期 | `AuthTokenExpired` | "登录已过期，请重新登录" | 前端触发刷新   |
| Token 无效  | `AuthFailed`       | "无效的token"    | 提示重新登录   |
| Cookie 缺失 | `AuthFailed`       | "缺少 refresh_token" | 跳转登录     |

> ⚠️ **注意**：前端需根据 `code` 字段判断错误类型，而非 `msg` 字段。

### Error Code 定义

```go
const (
    AuthFailed       ErorrCode = "AuthFailed"       // 通用认证失败
    AuthTokenExpired ErorrCode = "AuthTokenExpired" // Token 已过期
    // ...
)
```

### 安全特性

1. **Token 失效追踪**：通过 Redis 存储实现 Token 的主动失效
2. **滑动窗口**：刷新时使旧 Token 立即失效，防止重放攻击
3. **Token 撤销**：用户修改密码等操作可调用 `InvalidateTokens` 使该用户所有 Token 失效
4. **XSS 防护**：Refresh Token 存储在 HttpOnly Cookie 中，JavaScript 无法读取
5. **CSRF 防护**：SameSite=Strict 属性防止跨站请求伪造

## 前端接入指导

### Token 存储策略

| Token 类型      | 存储位置                      | 原因                  |
| ------------- | ------------------------- | ------------------- |
| Access Token  | 内存（Pinia）或 sessionStorage | 内存级存储，不易受 XSS 攻击    |
| Refresh Token | HttpOnly Cookie            | 不可被 JS 读取，防止 XSS 窃取 |

> ⚠️ **安全警告**：不要将 Access Token 存储在 localStorage 中，localStorage 易受 XSS（跨站脚本攻击）窃取。

### 前端代码示例

#### Vue 3 + Pinia

```typescript
// stores/auth.ts
import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: "",
    username: "",
  }),
  actions: {
    setAccessToken(token: string) {
      this.accessToken = token;
    },
    setUserInfo(username: string) {
      this.username = username;
    },
    clearAuth() {
      this.accessToken = "";
      this.username = "";
    },
  },
});
```

#### 请求拦截器配置

```typescript
// 请求拦截器 - 自动添加 Token
axios.interceptors.request.use((config) => {
  const accessToken = authStore.accessToken;
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

// 响应拦截器 - 处理认证错误
axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { code } = error.response?.data || {};

    switch (code) {
      case "AuthTokenExpired":
        // Token 过期 - 自动刷新（Cookie 自动携带，无需手动处理）
        await refreshToken();
        break;
      case "AuthFailed":
        // 认证失败 - 提示重新登录
        authStore.clearAuth();
        router.push("/login");
        break;
      default:
        return Promise.reject(error);
    }
  }
);
```

### Token 刷新流程

```typescript
// 自动刷新 Token
async function refreshToken() {
  try {
    // Cookie 自动携带 refresh_token，无需手动读取
    const response = await axios.post("/api/auth/refresh");
    // 更新内存中的 Access Token
    authStore.setAccessToken(response.data.data.access_token);
    // 新 Refresh Token 已通过 Set-Cookie 自动更新
  } catch (error) {
    // 刷新失败，跳转登录
    authStore.clearAuth();
    router.push("/login");
  }
}
```

### 登录流程

```typescript
// 登录
async function login(credentials) {
  try {
    const response = await axios.post("/api/auth/login", credentials);
    const { id, username, access_token } = response.data.data;

    authStore.setAccessToken(access_token);
    authStore.setUserInfo(username);

    // Refresh Token 已通过 Set-Cookie 自动设置，无需处理
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
}
```

### 登出流程

```typescript
// 登出
async function logout() {
  try {
    await axios.post("/api/auth/logout");
  } finally {
    // 清除本地状态，Cookie 由后端清除
    authStore.clearAuth();
    router.push("/login");
  }
}
```

### 开发环境注意事项

| 环境 | Secure | Cookie 行为 |
|------|--------|------------|
| 开发（localhost） | `false` | 浏览器允许发送 HTTP Cookie |
| 生产（HTTPS） | `true` | 仅在 HTTPS 连接下发送 |

### 安全建议

1. **XSS 防护**：前端需对用户输入进行严格过滤，防止 XSS 攻击窃取 Token
2. **CSRF 防护**：Refresh Token 放在 HttpOnly Cookie 中，配合 SameSite=Strict 属性防止 CSRF
3. **Token 有效期**：Access Token 有效期不宜过长，建议 2-4 小时
4. **监控告警**：对异常的 Token 使用行为进行监控和告警
5. **跨域配置**：确保前端和后端在同一域名下，或正确配置 Cookie 的 Domain 属性

### 配置参考

#### 后端配置示例

```go
// config.toml
[server]
env = "prod"
domain = "api.example.com"  // 跨子域时设置

[jwt]
accessTokenExpiration = 120  // 2小时
refreshTokenExpiration = 240 // 4小时
```

#### 生产环境 Cookie 设置

```go
c.SetSameSite(http.SameSiteStrictMode)
c.SetCookie(
    "refresh_token",
    refreshToken,
    7*24*3600,        // 7天
    "/",
    "api.example.com", // 生产环境设置域名
    true,             // Secure（仅 HTTPS）
    true,             // HttpOnly
)
```
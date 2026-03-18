--[[
    弹幕数据统计
    
    参数：
        KEYS[1]: 基础键名 (key)
        ARGV[1]: uid
        ARGV[2]: timestamps
        ARGV[3]: nickname
    
    逻辑：
        1. key + hll 后缀构成新 Key，使用 HyperLogLog 检查 uid+timestamps 是否重复
        2. 如果已存在则退出
        3. key + count 构成新 Key，操作 ZSet 数据，uid 为 member，分数默认为 0，分类计数 +1
        4. key + nickname 构成新 Key，操作 Set 数据，存放 "uid nickname" 值
    
    过期时间：24 小时（86400 秒）
]]--

local key = KEYS[1]
local uid = ARGV[1]
local timestamp = ARGV[2]
local nickname = ARGV[3]

-- 构建 HyperLogLog 键名
local hllKey = key .. ":hll"
-- 构建 ZSet 键名
local countKey = key .. ":count"
-- 构建 Set 键名（用于存储 nickname）
local nicknameKey = key .. ":nickname"
-- 过期时间：24 小时（86400 秒）
local expireTime = 86400

-- 组合 uid 和 timestamp 作为唯一标识
local uniqueValue = uid .. ":" .. timestamp

-- 1. 使用 HyperLogLog 检查是否存在
local exists = redis.call('PFADD', hllKey, uniqueValue)
-- 设置过期时间
redis.call('EXPIRE', hllKey, expireTime)

-- 2. 如果已存在 (返回 0)，则直接退出
if exists == 0 then
    return 0
end
-- 3. 不存在则进行计数操作
-- ZINCRBY 会在 member 不存在时自动创建，初始为 0 后再增加指定分数
redis.call('ZINCRBY', countKey, 1, uid)
-- 设置过期时间
redis.call('EXPIRE', countKey, expireTime)

-- 4. 存储 nickname 信息
-- 将 "uid nickname" 添加到 Set
redis.call('SADD', nicknameKey, uid .. " " .. nickname)
-- 设置过期时间
redis.call('EXPIRE', nicknameKey, expireTime)

-- 返回成功标记
return 1
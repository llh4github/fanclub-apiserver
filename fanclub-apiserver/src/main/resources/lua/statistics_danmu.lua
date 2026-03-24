--[[
    弹幕数据统计
    
    参数：
        KEYS[1]: 基础键名 (key)
        KEYS[2]: 分钟级时间
        ARGV[1]: uid
        ARGV[2]: timestamp
    
    逻辑：
        1. key + time + set 构成新 Key，使用 Set 检查 uid+timestamp 是否重复
        2. 如果已存在则退出
        3. key + count 构成新 Key，操作 ZSet 数据，uid 为 member，分数默认为 0，分类计数 +1
    
    过期时间：
        - set 数据：2 分钟（120 秒）
        - count 数据：24 小时（86400 秒）
]]--

local key = KEYS[1]
local time = KEYS[2]
local timedKey = key .. ":" .. time

local uid = ARGV[1]
local timestamp = ARGV[2]

-- 构建 set 键名（带分钟级时间）
local setKey = timedKey .. ":set"
-- 构建 ZSet 键名（用于分类计数）
local countKey = key
-- 过期时间：24 小时（86400 秒）
local expireTime = 86400

-- 过期时间：2 分钟（120 秒）
local expireTime2Min = 120

-- 组合 uid 和 timestamp 作为唯一标识
local uniqueValue = uid .. ":" .. timestamp

-- 1. 使用 Set 检查是否存在
local exists = redis.call('SADD', setKey, uniqueValue)
-- 设置过期时间（2 分钟）
redis.call('EXPIRE', setKey, expireTime2Min)

-- 2. 如果已存在 (返回 0)，则直接退出
if exists == 0 then
    return 0
end
-- 3. 不存在则进行计数操作
-- ZINCRBY 会在 member 不存在时自动创建，初始为 0 后再增加指定分数
redis.call('ZINCRBY', countKey, 1, uid)
-- 设置过期时间
redis.call('EXPIRE', countKey, expireTime)

-- 返回成功标记
return 1

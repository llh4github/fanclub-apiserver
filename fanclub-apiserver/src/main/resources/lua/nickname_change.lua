--[[
    昵称变更统计
    
    参数：
        KEYS[1]: 基础键名 (key)
        ARGV[1]: nickname
        ARGV[2]: uid
    
    逻辑：
        1. key + bloom 构成布隆过滤器的 Key
        2. 使用 nickname + uid 组成新值
        3. 用布隆过滤器检查该值是否存在
        4. 如果已存在则退出
        5. 如果不存在则将数据存储到以 key 为键名的 Hash 结构中
    
    过期时间：24 小时（86400 秒）
]]--

local key = KEYS[1]
local nickname = ARGV[1]
local uid = ARGV[2]

-- 构建布隆过滤器键名
local bloomKey = key .. ":bloom"
-- 组合 nickname 和 uid 作为唯一标识
local uniqueValue = nickname .. ":" .. uid
-- 过期时间：24 小时（86400 秒）
local expireTime = 86400

-- 1. 使用布隆过滤器检查是否存在
local exists = redis.call('BF.ADD', bloomKey, uniqueValue)

-- 2. 如果已存在 (返回 0)，则直接退出
if exists == 0 then
    return 0
end

-- 3. 不存在则将数据存储到 Hash 结构中
-- Hash 的 field 为 uid，value 为 nickname
redis.call('HSET', key, uid, nickname)
-- 设置过期时间
redis.call('EXPIRE', key, expireTime)

-- 返回成功标记
return 1
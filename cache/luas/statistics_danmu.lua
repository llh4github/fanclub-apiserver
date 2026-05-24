redis.register_function('statistics_danmu', function (keys, args)
    local key = keys[1]
    local time = keys[2]
    local uid = args[1]
    local timestamp = args[2]

    -- 构建 set 键名（带分钟级时间）
    local timedKey = key .. ":" .. time
    local setKey = timedKey .. ":set"
    -- 构建 ZSet 键名（用于分类计数）
    local countKey = key
    -- set 过期时间：61 秒
    local expireTimeSet = 61
    -- count 过期时间：24 小时（86400 秒）
    local expireTime = 86400

    -- 组合 uid 和 timestamp 作为唯一标识
    local uniqueValue = uid .. ":" .. timestamp

    -- 1. 使用 Set 检查是否存在
    local exists = redis.call('SADD', setKey, uniqueValue)

    -- 2. 如果已存在（返回 0），则直接退出
    if exists == 0 then
        return 0
    end

    -- 设置 set 过期时间
    redis.call('EXPIRE', setKey, expireTimeSet)

    -- 3. 不存在则进行计数操作
    -- ZINCRBY 会在 member 不存在时自动创建，初始为 0 后再增加指定分数
    redis.call('ZINCRBY', countKey, 1, uid)
    -- 设置 count 过期时间
    redis.call('EXPIRE', countKey, expireTime)

    -- 返回成功标记
    return 1
end)

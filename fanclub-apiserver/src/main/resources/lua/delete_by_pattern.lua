--[[
    按模式批量删除 Redis Key
    
    参数：
        KEYS[1]: key 的模式字符串，支持通配符 (如 "fanclub-statistics:danmu*")
    
    逻辑：
        1. 使用 SCAN 命令迭代匹配模式的 key
        2. 使用 UNLINK 异步删除匹配的 key(非阻塞)
        3. 每次迭代扫描 1000 个 key
        4. 返回删除的 key 数量
    
    返回值：
        - 正整数：成功删除的 key 数量
        - 0：未找到匹配的 key
        - -1：执行出错
        - -2：模式不安全，拒绝删除
    
    注意：
        - 使用 UNLINK 而非 DEL，避免阻塞 Redis 主线程
        - 使用 SCAN 而非 KEYS，避免在大数据量时阻塞
        - 禁止使用空模式或 "*" 通配符，防止误删所有数据
]]--

-- 初始化游标为 0，表示开始扫描
local cursor = 0
-- 从第一个 KEYS 参数获取 key 的模式
local pattern = KEYS[1]
-- 计数器，记录删除的 key 数量
local count = 0

-- 安全检查：避免误删所有数据
if not pattern or pattern == "" then
    return -2  -- 返回错误码 -2 表示模式为空
end

-- 额外检查：如果模式仅为 "*"，视为危险模式，拒绝执行
if pattern == "*" then
    return -2
end

-- 循环扫描，直到游标回到 0(扫描完成)
repeat
    -- 使用 SCAN 命令迭代 key
    -- 参数：当前游标、MATCH 模式、COUNT 每次返回的数量
    -- pcall 用于捕获可能的错误
    local ok, res = pcall(redis.call, 'SCAN', cursor, 'MATCH', pattern, 'COUNT', 1000)
    if not ok then
        return -1
    end

    -- 更新游标为下一次迭代的起始位置
    cursor = tonumber(res[1])
    -- 获取本次扫描到的 key 列表
    local keys = res[2]

    -- 如果有扫描到的 key，则逐个删除
    if keys then
        for i = 1, #keys do
            -- 使用 UNLINK 异步删除 key，不会阻塞 Redis 主线程
            redis.call('UNLINK', keys[i])
            count = count + 1
        end
    end
until cursor == 0

-- 返回删除的 key 总数
return count

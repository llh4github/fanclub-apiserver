--[[
    按模式批量删除 Redis Key
    
    参数：
        KEYS[1..n]: 多个 key 的模式字符串，支持通配符 (如 "fanclub-statistics:danmu*")
    
    逻辑：
        1. 遍历所有传入的 KEYS 参数
        2. 对每个模式使用 SCAN 命令迭代匹配的 key
        3. 使用 UNLINK 异步删除匹配的 key(非阻塞)
        4. 每次迭代扫描 1000 个 key
        5. 返回删除的 key 数量
    
    返回值：
        - 正整数：成功删除的 key 数量（部分成功）
        - 0：未找到匹配的 key 或所有模式都不合法
        - -1：执行出错
    
    注意：
        - 使用 UNLINK 而非 DEL，避免阻塞 Redis 主线程
        - 使用 SCAN 而非 KEYS，避免在大数据量时阻塞
        - 禁止使用空模式或 "*" 通配符，会自动跳过这些不安全的模式
        - 支持传入多个模式，会依次处理所有模式
        - 采用部分成功模式：不合法的模式会被跳过，不影响其他模式的执行
]]--

-- 初始化游标为 0，表示开始扫描
local cursor = 0
-- 计数器，记录删除的 key 数量
local total_count = 0

-- 安全检查：至少需要一个模式
if #KEYS == 0 then
    return 0  -- 没有提供模式，返回 0
end

-- 遍历所有传入的模式
for i = 1, #KEYS do
    local pattern = KEYS[i]
    
    -- 跳过不安全的模式：空模式或 "*"
    if pattern and pattern ~= "" and pattern ~= "*" then
        -- 重置游标，开始扫描当前模式
        cursor = 0
        
        -- 循环扫描，直到游标回到 0(扫描完成)
        repeat
            -- 使用 SCAN 命令迭代 key
            -- 参数：当前游标、MATCH 模式、COUNT 每次返回的数量
            -- pcall 用于捕获可能的错误
            local ok, res = pcall(redis.call, 'SCAN', cursor, 'MATCH', pattern, 'COUNT', 1000)
            if ok then
                -- 更新游标为下一次迭代的起始位置
                cursor = tonumber(res[1])
                -- 获取本次扫描到的 key 列表
                local keys = res[2]

                -- 如果有扫描到的 key，则逐个删除
                if keys then
                    for j = 1, #keys do
                        -- 使用 UNLINK 异步删除 key，不会阻塞 Redis 主线程
                        redis.call('UNLINK', keys[j])
                        total_count = total_count + 1
                    end
                end
            end
        until cursor == 0
    end
end

-- 返回删除的 key 总数（可能为 0，表示没有删除任何 key）
return total_count

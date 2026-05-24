redis.register_function('scan_unlink', function (keys, args)
    local deleted = 0
    for i = 1, #args do
        local pattern = args[i]
        local cursor = "0"
        repeat
            local result = redis.call('SCAN', cursor, 'MATCH', pattern)
            cursor = result[1]
            local keys = result[2]
            if #keys > 0 then
                redis.call('UNLINK', unpack(keys))
                deleted = deleted + #keys
            end
        until cursor == "0"
    end
    return deleted
end)
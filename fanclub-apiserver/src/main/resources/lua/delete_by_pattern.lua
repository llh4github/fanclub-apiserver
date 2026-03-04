-- delete_by_pattern.lua
local cursor = 0
local pattern = ARGV[1]
local count = 0

if not pattern then
    return 0
end

repeat
    local ok, res = pcall(redis.call, 'SCAN', cursor, 'MATCH', pattern, 'COUNT', 1000)
    if not ok then
        return -1
    end

    cursor = tonumber(res[1])
    local keys = res[2]

    if keys then
        for i = 1, #keys do
            redis.call('UNLINK', keys[i])
            count = count + 1
        end
    end
until cursor == 0

return count

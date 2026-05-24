#!lua name=fanclub_apiserver

redis.register_function('set_expire', function (keys, args)
    local expireSeconds = tonumber(args[1])
    if not expireSeconds then
        return 0
    end
    
    local count = 0
    for i = 1, #keys do
        local key = keys[i]
        redis.call('EXPIRE', key, expireSeconds)
        count = count + 1
    end
    return count
end)
local stock = redis.call('get', 'food:' .. KEYS[1])
local quantity = tonumber(ARGV[1])
if stock - quantity < 0 then
    return -1
else
    redis.call('decrby', 'food:' .. KEYS[1], ARGV[1])
    return stock - quantity
end
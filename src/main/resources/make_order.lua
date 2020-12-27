-- Make order.
-- @param KEYS[1] id of cart
-- @param KEYS[2] actk
-- @param KEYS[3] id of order
-- @param KEYS[4] id of user
local actk = redis.call("get", "c:"..KEYS[1])
if actk then
  if KEYS[2] == actk then
    local menu = redis.call("hgetall", "c:f"..KEYS[1])
    local foods = redis.call("hgetall", "f")
    local valid = true
    for fid,cnt in ipairs(menu) do
      if foods[fid] - cnt < 0 then
        valid = false
      end
    end
    if valid then
      for fid,cnt in ipairs(menu) do
        redis.call("hset", "f", foods[fid]-cnt)
        redis.call("hset", "o:f:"..KEYS[3], fid, cnt)
      end
      redis.call("set", "o:"..KEYS[3], KEYS[4])
      return "{\"id\":\""..KEYS[4].."\"}"
    else
      return "{\"code\": \"FOOD_OUT_OF_STOCK\",\"message\":\"食物库存不足\"}"
    end
  else
    return "{\"code\": \"NOT_AUTHORIZED_TO_ACCESS_CART\",\"message\":\"无权限访问指定的篮子\"}"
else
  return "{\"code\": \"CART_NOT_FOUND\",\"message\":\"篮子不存在\"}"
end

local actk = redis.call("get", "c:"..KEYS[1])
		local user_id = redis.call("get", "u:"..KEYS[2])
		if redis.call("get", "u:o:"..user_id) == false then
		  if actk then
		    if KEYS[2] == actk then
		      local menu = redis.call("hgetall", "c:f:"..KEYS[1])
		      if menu then
		        local valid = true
		        local foods = {}
		        for i = 1, table.getn(menu), 2 do
		          foods[menu[i]] = tonumber(redis.call("hget", "f", menu[i]))
		          if foods[menu[i]] <= 0 or foods[menu[i]] - tonumber(menu[i+1]) < 0 then
		            valid = false
		          end
		        end
		        if valid then
		          for i = 1, table.getn(menu), 2 do
		            local new_stock = foods[menu[i]] - tonumber(menu[i+1])
		            redis.call("hset", "f", menu[i], new_stock)
		            redis.call("hset", "o:f:"..KEYS[3], menu[i], menu[i+1])
		          end
		          redis.call("set", "oid:"..KEYS[3], user_id)
		          redis.call("set", "u:o:"..user_id, KEYS[3])
		          return {200, "{\"id\":\""..KEYS[3].."\"}"}
		        else
		          return {403, "{\"code\": \"FOOD_OUT_OF_STOCK\",\"message\":\"食物库存不足\"}"}
		        end
		      else
		        return {403, "{\"code\": \"NO_FOOD_IN_CART\",\"message\":\"篮子中无食物\"}"}
		      end
		    else
		      return {401, "{\"code\": \"NOT_AUTHORIZED_TO_ACCESS_CART\",\"message\":\"无权限访问指定的篮子\"}"}
		    end
		  else
		    return {404, "{\"code\": \"CART_NOT_FOUND\",\"message\":\"篮子不存在\"}"}
		  end
		else
		  return {403, "{\"code\": \"ORDER_OUT_OF_LIMIT\",\"message\":\"每个用户只能下一单\"}"}
		end
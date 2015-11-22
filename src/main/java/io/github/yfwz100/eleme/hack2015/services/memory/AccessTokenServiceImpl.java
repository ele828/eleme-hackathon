package io.github.yfwz100.eleme.hack2015.services.memory;

import io.github.yfwz100.eleme.hack2015.models.Session;
import io.github.yfwz100.eleme.hack2015.models.User;
import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;
import io.github.yfwz100.eleme.hack2015.services.Cache;
import io.github.yfwz100.eleme.hack2015.services.ContextService;
import io.github.yfwz100.eleme.hack2015.services.exceptions.UserNotFoundException;

import java.util.UUID;

/**
 * The mock of access token service.
 *
 * @author yfwz100
 */
public class AccessTokenServiceImpl implements AccessTokenService {

    private static final Cache cache = ContextService.getCache();

    @Override
    public Session checkUserPassword(String username, String password) throws UserNotFoundException {
        User user = cache.getUser(username);
        if (user != null && user.getPass().equals(password)) {
            String accessToken = generateAccessToken();
            Session session = new Session(user, accessToken);
            cache.addSession(session);
            return session;
        } else {
            throw new UserNotFoundException();
        }
    }

    @Override
    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Session checkAccessToken(String accessToken) {
        return cache.getSession(accessToken);
    }

}
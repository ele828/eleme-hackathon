package io.github.yfwz100.eleme.hack2015.services.mock;

import io.github.yfwz100.eleme.hack2015.services.AccessTokenService;

/**
 * The mock of access token service.
 *
 * @author yfwz100
 */
public class AccessTokenServiceMock implements AccessTokenService {
    @Override
    public boolean checkUserPassword(String username, String password) {
        return true;
    }

    @Override
    public String generateAccessToken(String username, String password) {
        return "aaa";
    }

    @Override
    public boolean checkAccessToken(String accessToken) {
        return true;
    }
}

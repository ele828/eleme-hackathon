package io.github.yfwz100.eleme.hack2015.services;

/**
 * The access token utility.
 *
 * @author yfwz100
 */
public interface AccessTokenService {

    boolean checkUserPassword(String username, String password);

    String generateAccessToken(String username, String password);

    boolean checkAccessToken(String accessToken);
}

package io.github.yfwz100.eleme.hack2015.services;

import io.github.yfwz100.eleme.hack2015.services.exceptions.UserNotFoundException;
import io.github.yfwz100.eleme.hack2015.models.Session;

/**
 * The interface related to access token.
 *
 * @author yfwz100
 */
public interface AccessTokenService {

    /**
     * Check username and password.
     *
     * @param username the user name.
     * @param password the password.
     * @return the session.
     * @throws UserNotFoundException if username and password is not matched.
     */
    Session checkUserPassword(String username, String password) throws UserNotFoundException;

    /**
     * Generate the access token.
     *
     * @return the string representation of the access token.
     */
    String generateAccessToken();

    /**
     * Check the validation of access token.
     *
     * @param accessToken the access token to check.
     * @return the session.
     */
    Session checkAccessToken(String accessToken);
}

package io.github.yfwz100.eleme.hack2015.models;

/**
 * The authorized user.
 *
 * @author yfwz100
 */
public class Session {

    private User user;
    private String accessToken;

    public Session(User user, String accessToken) {
        this.user = user;
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

package Server;

/**
 * Parses the MTCG bearer token ("Bearer <username>-mtcgToken", format given by the specification).
 * Only tokens that exactly match this format are accepted; previously everything before the
 * first '-' was taken as the username (e.g. "Bearer admin-whatever").
 */
public final class AuthToken {

    private static final String PREFIX = "Bearer ";
    private static final String SUFFIX = "-mtcgToken";

    private AuthToken() { }

    /**
     * @return the username, or null if the header is missing or malformed
     */
    public static String usernameFrom(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        String header = authorizationHeader.trim();
        if (!header.startsWith(PREFIX) || !header.endsWith(SUFFIX)) {
            return null;
        }
        String username = header.substring(PREFIX.length(), header.length() - SUFFIX.length());
        if (username.isEmpty() || username.length() > 64 || !username.matches("[A-Za-z0-9_.]+")) {
            return null;
        }
        return username;
    }
}

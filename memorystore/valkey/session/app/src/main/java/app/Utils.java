/**
 * Utility class for generating secure tokens and managing cookies.
 */

package app;

import jakarta.servlet.http.Cookie;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;

public class Utils {

  public static String generateToken(int tokenByteLength) {
    // SecureRandom ensures cryptographic security
    SecureRandom secureRandom = new SecureRandom();
    byte[] randomBytes = new byte[tokenByteLength];
    secureRandom.nextBytes(randomBytes);

    // Encode the random bytes into a URL-safe Base64 string
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  public static String getTokenFromCookie(Cookie[] cookies) {
    if (cookies == null) {
      return null;
    }
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(Global.TOKEN_COOKIE_NAME)) {
        return cookie.getValue();
      }
    }
    return null;
  }

  public static Cookie createCookie(String token) {
    Cookie cookie = new Cookie(Global.TOKEN_COOKIE_NAME, token);
    cookie.setPath("/"); // Available across the app
    cookie.setMaxAge(Global.TOKEN_EXPIRATION); // Set expiration

    return cookie;
  }

  public static Timestamp getFutureTimestamp(long seconds) {
    long currentTime = System.currentTimeMillis();
    long futureTime = currentTime + (seconds * 1000);
    return new Timestamp(futureTime);
  }
}

package sonia.scm.security;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * XsrfExcludes can be used to define request uris which are excluded from xsrf validation.
 * @since 2.28.0
 */
@Singleton
public class XsrfExcludes {

  private final Set<String> excludes = new HashSet<>();

  /**
   * Exclude the given request uri from xsrf validation.
   * @param requestUri request uri
   */
  public void add(String requestUri) {
    excludes.add(requestUri);
  }

  /**
   * Include prior excluded request uri to xsrf validation.
   * @param requestUri request uri
   * @return {@code true} is uri was excluded
   */
  @CanIgnoreReturnValue
  public boolean remove(String requestUri) {
    return excludes.remove(requestUri);
  }

  /**
   * Returns {@code true} if the request uri is excluded from xsrf validation.
   * @param requestUri request uri
   * @return {@code true} if uri is excluded
   */
  public boolean contains(String requestUri) {
    return excludes.contains(requestUri);
  }
}

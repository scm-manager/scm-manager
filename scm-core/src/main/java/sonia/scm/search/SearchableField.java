package sonia.scm.search;

import com.google.common.annotations.Beta;

/**
 * A field of a {@link SearchableType}.
 *
 * @since 2.23.0
 */
@Beta
public interface SearchableField {
  String getName();

  Class<?> getType();
}

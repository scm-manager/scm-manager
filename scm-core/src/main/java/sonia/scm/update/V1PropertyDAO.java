package sonia.scm.update;

/**
 * Use this to access old properties from an instance of SCM-Manager v1.
 */
public interface V1PropertyDAO {
  /**
   * Creates an instance of a property reader to process old properties.
   * @param reader The reader for the origin of the properties (for example
   *   {@link V1PropertyReader#REPOSITORY_PROPERTY_READER} for properties of repositories).
   * @return The reader instance.
   */
  V1PropertyReader.Instance getProperties(V1PropertyReader reader);
}

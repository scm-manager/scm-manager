package sonia.scm;


/**
 * This is a reduced form of a model object.
 * It can be used as search result to avoid returning the whole object properties.
 *
 * @author Mohamed Karray
 */
public interface ReducedModelObject {

  String getId();

  String getDisplayName();
}

package sonia.scm.web;

import sonia.scm.plugin.ExtensionPoint;

/**
 * Implementing this extension point you can post process json response objects.
 * To do so, you get a {@link JsonEnricherContext} with the complete json tree,
 * that can be modified.
 */
@ExtensionPoint
public interface JsonEnricher {

  void enrich(JsonEnricherContext context);

}

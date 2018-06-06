package sonia.scm.web;

import sonia.scm.plugin.ExtensionPoint;

@ExtensionPoint
public interface JsonEnricher {

  void enrich(JsonEnricherContext context);

}

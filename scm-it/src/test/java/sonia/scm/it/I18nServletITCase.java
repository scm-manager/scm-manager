package sonia.scm.it;

import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;

import static org.assertj.core.api.Assertions.assertThat;

public class I18nServletITCase {

  @Test
  public void shouldGetCollectedPluginTranslations() {
    ScmRequests.start()
      .requestPluginTranslations("de")
      .assertStatusCode(200)
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-git-plugin")
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-hg-plugin")
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-svn-plugin");
  }
}

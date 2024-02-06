/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.template;

import java.io.IOException;
import java.io.Reader;
import java.util.Locale;

/**
 * The {@link TemplateEngine} searches for {@link Template}s and prepares the
 * template for the rendering process.
 *
 * @since 1.19
 *
 * @apiviz.uses sonia.scm.template.Template
 */
public interface TemplateEngine
{

  /**
   * Returns the template associated with the given path. The template engine
   * will search the template in the folder of the web application and in
   * the classpath. This method will return null,
   * if no template could be found for the given path.
   *
   *
   * @param templatePath path of the template
   *
   * @return template associated with the given path or null
   *
   * @throws IOException
   */
  Template getTemplate(String templatePath) throws IOException;

  /**
   * Returns the template associated with the given path and the given language
   * or the default version. To do this, the template path will be extended
   * with the language. For example `my-template.type` will be extended to
   * `my-template_en.type`, if the language is {@link Locale#ENGLISH}. If no
   * dedicated template can be found, the template without the extension will
   * be used.
   * <p>The template engine
   * will search the template in the folder of the web application and in
   * the classpath. This method will return null,
   * if no template could be found for the given path.
   *
   *
   * @param templatePath path of the template
   * @param locale the preferred language
   *
   * @return template associated with the given path, extended by the language
   *   if found, or null
   *
   * @throws IOException
   */
  default Template getTemplate(String templatePath, Locale locale) throws IOException {
    String templatePathWithLanguage = extendWithLanguage(templatePath, locale);
    Template templateForLanguage = getTemplate(templatePathWithLanguage);
    if (templateForLanguage == null) {
      return getTemplate(templatePath);
    } else {
      return templateForLanguage;
    }
  }

  /**
   * Creates a template of the given reader. Note some template implementations 
   * will cache the template by its id.
   *
   *
   * @param templateIdentifier id of the template
   * @param reader template reader
   *
   * @return template created from the reader
   *
   * @throws IOException
   * 
   * @since 1.22
   */
  Template getTemplate(String templateIdentifier, Reader reader)
    throws IOException;

  TemplateType getType();

  static String extendWithLanguage(String templatePath, Locale locale) {
    int lastIndexOfDot = templatePath.lastIndexOf('.');
    String filename = templatePath.substring(0, lastIndexOfDot);
    String extension = templatePath.substring(lastIndexOfDot);
    return filename + "_" + locale.getLanguage() + extension;
  }
}

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

package sonia.scm.cli;

import com.google.common.base.MoreObjects;
import picocli.CommandLine;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class TemplateCommand {

  @CommandLine.Option(names = {"--template", "-t"}, paramLabel = "TEMPLATE", description = "Specify rendering template")
  private String template;

  private final CliContext context;
  private final TemplateEngine templateEngine;

  TemplateCommand(CliContext context, TemplateEngineFactory templateEngineFactory) {
    this.context = context;
    this.templateEngine = templateEngineFactory.getDefaultEngine();
  }

  public void exec(String defaultTemplate, Map<String, Object> model) {
    try (OutputStreamWriter osw = new OutputStreamWriter(context.getStdout())) {
      Template tpl = templateEngine.getTemplate(getClass().getName(), new StringReader(MoreObjects.firstNonNull(template, defaultTemplate)));
      tpl.execute(osw, createModel(model));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Object createModel(Map<String, Object> model) {
    Map<String, Object> finalModel = new HashMap<>(model);
    finalModel.put("lf", "\n");
    return Collections.unmodifiableMap(finalModel);
  }
}

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

package sonia.scm.plugin;

import org.eclipse.transformer.AppOption;
import org.eclipse.transformer.TransformOptions;
import org.eclipse.transformer.Transformer;
import org.eclipse.transformer.jakarta.JakartaTransform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public class PluginTransformer {

  private PluginTransformer() {
  }

  public static void transform(Path pluginPath) {
    Transformer transformer = new Transformer(createOptions(pluginPath.toString()));
    transformer.inputPath = pluginPath.resolve("classes").toString();
    transformer.outputName = pluginPath + "-transformed";
    Transformer.ResultCode resultCode = transformer.run();

    if (resultCode.ordinal() == 0) {
      try {
        try (Stream<Path> paths = Files.walk(Path.of(transformer.inputPath))) {
          paths
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
        Files.move(Path.of(transformer.outputPath), Path.of(transformer.inputPath), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        throw new PluginTransformException("Failed to overwrite transformed classes", e);
      }
    } else {
      throw new PluginTransformException(
        String.format("Failed to transform downloaded plugin: %s",
         pluginPath)
      );
    }
  }

  private static TransformOptions createOptions(String inputFileName) {
    return new TransformOptions() {
      @Override
      public boolean hasOption(AppOption option) {
        if (option == AppOption.OVERWRITE) {
          return true;
        }
        return TransformOptions.super.hasOption(option);
      }

      @Override
      public String getDefaultValue(AppOption option) {
        return JakartaTransform.getOptionDefaults().get(option.getLongTag());
      }

      @Override
      public Function<String, URL> getRuleLoader() {
        return JakartaTransform.getRuleLoader();
      }

      @Override
      public String getInputFileName() {
        return inputFileName;
      }

      @Override
      public String getOutputFileName() {
        // Ignore this because we want to keep the input file name
        return null;
      }
    };
  }
}

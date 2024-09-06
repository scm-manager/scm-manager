/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.plugin;

import org.eclipse.transformer.AppOption;
import org.eclipse.transformer.TransformOptions;
import org.eclipse.transformer.Transformer;
import org.eclipse.transformer.jakarta.JakartaTransform;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.eclipse.transformer.Transformer.ResultCode.SUCCESS_RC;

public class PluginTransformer {

  private PluginTransformer() {
  }

  public static void transform(Path pluginPath) {
    Transformer transformer = new Transformer(LoggerFactory.getLogger(Transformer.class), createOptions(pluginPath.toString()));
    transformer.inputPath = pluginPath.resolve("classes").toString();
    transformer.outputName = pluginPath + "-transformed";
    Transformer.ResultCode resultCode = transformer.run();

    if (resultCode == SUCCESS_RC) {
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
        String.format("Failed to transform downloaded plugin %s: %s", pluginPath, resultCode)
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

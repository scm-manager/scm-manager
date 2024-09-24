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

package sonia.scm.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;
import sonia.scm.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ServerConfigParser {

  private static final String CONFIGURATION_FILE = "/config.yml";

  ServerConfigParser() {
  }

  ServerConfigYaml parse() {
    return parse(this.getClass().getResource(CONFIGURATION_FILE));
  }

  ServerConfigYaml parse(URL configFile) {
    if (configFile == null) {
      // TODO add link
      throw new ConfigurationException("""
        Could not find config.yml.
                    
        If you have upgraded from an older SCM-Manager version, you have to migrate your server-config.xml 
        to the new format using the official instructions: 
                  
        <link>
        """);
    }
    try (InputStream is = configFile.openStream()) {
      Representer representer = new Representer(new DumperOptions());
      representer.getPropertyUtils().setSkipMissingProperties(true);
      return new Yaml(representer).loadAs(is, ServerConfigYaml.class);
    } catch (IOException e) {
      throw new ConfigurationException("Could not parse config.yml", e);
    }
  }
}

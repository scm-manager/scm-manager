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

package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.RepositoryManager;

import javax.inject.Inject;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class HgGlobalConfigToHgGlobalConfigDtoMapper extends BaseMapper<HgGlobalConfig, HgGlobalGlobalConfigDto> {

  @Inject
  private HgConfigLinks links;
  @Inject
  private RepositoryManager repositoryManager;

  @VisibleForTesting
  void setLinks(HgConfigLinks links) {
    this.links = links;
  }

  @VisibleForTesting
  void setRepositoryManager(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @AfterMapping
  void appendLinks(HgGlobalConfig config, @MappingTarget HgGlobalGlobalConfigDto target) {
    HgConfigLinks.GlobalConfigLinks configLinks = links.global();
    Links.Builder linksBuilder = linkingTo().self(configLinks.get());
    if (ConfigurationPermissions.write(config).isPermitted()) {
      linksBuilder.single(link("update", configLinks.update()));
      linksBuilder.single(link("autoConfiguration", configLinks.autoConfigure()));
    }
    target.add(linksBuilder.build());
    target.setAllowDisable(repositoryManager.getAll().stream().noneMatch(r -> r.getType().equals("hg")));
  }
}

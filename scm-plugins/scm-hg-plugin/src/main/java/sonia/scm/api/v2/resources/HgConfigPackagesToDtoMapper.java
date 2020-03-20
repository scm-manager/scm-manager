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

import de.otto.edison.hal.Links;
import lombok.Getter;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.installer.HgPackage;
import sonia.scm.installer.HgPackages;

import javax.inject.Inject;
import java.util.List;

import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class HgConfigPackagesToDtoMapper  {

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  public HgConfigPackagesDto map(HgPackages hgpackages) {
    return map(new HgPackagesNonIterable(hgpackages));
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  /* Favor warning "Unmapped target property: "attributes", to packages[].hgConfigTemplate"
     Over error "Unknown property "packages[].hgConfigTemplate.attributes"
     @Mapping(target = "packages[].hgConfigTemplate.attributes", ignore = true) // Also not for nested DTOs
   */
  protected abstract HgConfigPackagesDto map(HgPackagesNonIterable hgPackagesNonIterable);

  @AfterMapping
  void appendLinks(@MappingTarget HgConfigPackagesDto target) {
    Links.Builder linksBuilder = linkingTo().self(createSelfLink());
    target.add(linksBuilder.build());
  }

  private String createSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("getPackagesResource").parameters().href();
  }

  /**
   * Unfortunately, HgPackages is iterable, HgConfigPackagesDto does not need to be iterable and MapStruct refuses to
   * map an iterable to a non-iterable. So use this little non-iterable "proxy".
   */
  @Getter
  static class HgPackagesNonIterable {
    private List<HgPackage> packages;

    HgPackagesNonIterable(HgPackages hgPackages) {
      this.packages = hgPackages.getPackages();
    }
  }
}

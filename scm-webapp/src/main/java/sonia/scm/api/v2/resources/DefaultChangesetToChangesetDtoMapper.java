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

import com.google.common.base.Strings;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Contributor;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Signature;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.security.gpg.PublicKeyResource;
import sonia.scm.security.gpg.PublicKeyStore;
import sonia.scm.security.gpg.RawGpgKey;
import sonia.scm.web.EdisonHalAppender;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.Optional.ofNullable;

@Mapper
public abstract class DefaultChangesetToChangesetDtoMapper extends HalAppenderMapper implements InstantAttributeMapper, ChangesetToChangesetDtoMapper {

  @Inject
  private RepositoryServiceFactory serviceFactory;

  @Inject
  private ResourceLinks resourceLinks;

  @Inject
  private BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  @Inject
  private ChangesetToParentDtoMapper changesetToParentDtoMapper;

  @Inject
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @Inject
  private PublicKeyStore publicKeyStore;

  abstract ContributorDto map(Contributor contributor);

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  abstract SignatureDto map(Signature signature);

  abstract PersonDto map(Person person);

  @ObjectFactory
  SignatureDto createDto(Signature signature) {
    Optional<RawGpgKey> key = ofNullable(emptyToNull(signature.getKeyId())).flatMap(publicKeyStore::findById);
    if (signature.getType().equals("gpg") && key.isPresent()) {
      final Links.Builder linkBuilder =
        linkingTo()
          .single(link("rawKey", new LinkBuilder(scmPathInfoStore.get(), PublicKeyResource.class)
            .method("findByIdGpg")
            .parameters(signature.getKeyId())
            .href()));

      return new SignatureDto(linkBuilder.build());
    }
    return new SignatureDto();
  }

  @ObjectFactory
  ChangesetDto createDto(@Context Repository repository, Changeset source) {
    String namespace = repository.getNamespace();
    String name = repository.getName();

    Embedded.Builder embeddedBuilder = embeddedBuilder();

    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.changeset().self(repository.getNamespace(), repository.getName(), source.getId()))
      .single(link("diff", resourceLinks.diff().self(namespace, name, source.getId())))
      .single(link("sources", resourceLinks.source().self(namespace, name, source.getId())))
      .single(link("modifications", resourceLinks.modifications().self(namespace, name, source.getId())));

    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (repositoryService.isSupported(Command.TAGS)) {
        embeddedBuilder.with("tags", tagCollectionToDtoMapper.getMinimalEmbeddedTagDtoList(namespace, name, source.getTags()));
      }
      if (repositoryService.isSupported(Command.TAG) && RepositoryPermissions.push(repository).isPermitted()) {
        linksBuilder.single(link("tag", resourceLinks.tag().create(namespace, name)));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        embeddedBuilder.with("branches", branchCollectionToDtoMapper.getBranchDtoList(repository,
          getListOfObjects(source.getBranches(), branchName -> Branch.normalBranch(branchName, source.getId()))));
      }
      if (repositoryService.isSupported(Command.DIFF_RESULT)) {
        linksBuilder.single(link("diffParsed", resourceLinks.diff().parsed(namespace, name, source.getId())));
      }
    }
    embeddedBuilder.with("parents", getListOfObjects(source.getParents(), parent -> changesetToParentDtoMapper.map(new Changeset(parent, 0L, null), repository)));

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), source, repository);

    return new ChangesetDto(linksBuilder.build(), embeddedBuilder.build());
  }

  private <T> List<T> getListOfObjects(List<String> list, Function<String, T> mapFunction) {
    return list
      .stream()
      .map(mapFunction)
      .collect(Collectors.toList());
  }
}

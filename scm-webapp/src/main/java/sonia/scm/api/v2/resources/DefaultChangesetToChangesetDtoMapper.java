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

package sonia.scm.api.v2.resources;

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
import sonia.scm.repository.Feature;
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
      if (repositoryService.isSupported(Command.TAGS) && repositoryService.isSupported(Feature.TAGS_FOR_REVISION)) {
        linksBuilder.single(link("containedInTags", resourceLinks.tag().getForChangeset(namespace, name, source.getId())));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        embeddedBuilder.with("branches", branchCollectionToDtoMapper.getBranchDtoList(repository,
          getListOfObjects(source.getBranches(), branchName -> Branch.normalBranch(branchName, source.getId()))));
      }
      if (repositoryService.isSupported(Command.DIFF_RESULT)) {
        linksBuilder.single(link("diffParsed", resourceLinks.diff().parsed(namespace, name, source.getId())));
      }
      if (
        repositoryService.isSupported(Command.REVERT) &&
        RepositoryPermissions.push(repository).isPermitted() &&
        source.getParents().size() == 1) {
        linksBuilder.single(link("revert", resourceLinks.changeset().revert(namespace, name, source.getId())));
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

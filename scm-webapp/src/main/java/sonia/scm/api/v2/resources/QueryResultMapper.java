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

import com.damnhandy.uri.template.UriTemplate;
import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.Data;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryCoordinates;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.search.Hit;
import sonia.scm.search.QueryResult;
import sonia.scm.web.EdisonHalAppender;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;

@Mapper
public abstract class QueryResultMapper extends HalAppenderMapper {

  @Inject
  private RepositoryManager repositoryManager;

  @Inject
  private ResourceLinks resourceLinks;

  @VisibleForTesting
  void setRepositoryManager(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }

  public abstract QueryResultDto map(@Context SearchParameters params, QueryResult result);

  public abstract EmbeddedRepositoryDto map(Repository repository);

  @AfterMapping
  void setPageValues(@MappingTarget QueryResultDto dto, QueryResult result, @Context SearchParameters params) {
    int totalHits = (int) result.getTotalHits();
    dto.setPageTotal(computePageTotal(totalHits, params.getPageSize()));
    dto.setPage(params.getPage());
  }

  @Nonnull
  @ObjectFactory
  EmbeddedRepositoryDto createDto(Repository repository) {
    Links.Builder links = linkingTo();
    links.self(resourceLinks.repository().self(repository.getNamespace(), repository.getName()));
    Embedded.Builder embedded = Embedded.embeddedBuilder();

    HalEnricherContext context = HalEnricherContext.builder()
      .put(RepositoryCoordinates.class, repository)
      .build();

    applyEnrichers(context, new EdisonHalAppender(links, embedded), RepositoryCoordinates.class);
    return new EmbeddedRepositoryDto(links.build(), embedded.build());
  }

  @Nonnull
  @ObjectFactory
  QueryResultDto createDto(@Context SearchParameters params, QueryResult result) {
    int totalHits = (int) result.getTotalHits();
    Links.Builder links = links(params, totalHits);
    Embedded.Builder embedded = hits(result);
    applyEnrichers(new EdisonHalAppender(links, embedded), result);
    return new QueryResultDto(links.build(), embedded.build());
  }

  @Nonnull
  private QueryResultDto createDto(SearchParameters params, QueryResult result, int totalHits) {
    Links.Builder links = links(params, totalHits);
    Embedded.Builder embedded = hits(result);
    applyEnrichers(new EdisonHalAppender(links, embedded), result);
    return new QueryResultDto(links.build(), embedded.build());
  }

  private Links.Builder links(SearchParameters params, int totalHits) {
    NumberedPaging paging = zeroBasedNumberedPaging(params.getPage(), params.getPageSize(), totalHits);

    UriTemplate uriTemplate = fromTemplate(params.getSelfLink() + "{?q,page,pageSize}");
    uriTemplate.set("q", params.getQuery());

    return linkingTo()
      .with(paging.links(
        uriTemplate,
        EnumSet.allOf(PagingRel.class))
      );
  }

  @Nonnull
  private Embedded.Builder hits(QueryResult result) {
    List<HitDto> hits = result.getHits()
      .stream()
      .map(hit -> map(result, hit))
      .collect(Collectors.toList());
    return Embedded.embeddedBuilder().with("hits", hits);
  }

  @ObjectFactory
  protected HitDto createHitDto(@Context QueryResult queryResult, Hit hit) {
    Links.Builder links = linkingTo();
    Embedded.Builder embedded = Embedded.embeddedBuilder();
    hit.getRepositoryId().map(this::repository).ifPresent(r -> embedded.with("repository", r));
    applyEnrichers(new EdisonHalAppender(links, embedded), hit, queryResult);
    return new HitDto(links.build(), embedded.build());
  }

  @Nullable
  private HalRepresentation repository(String id) {
    Repository repository = repositoryManager.get(id);
    if (repository != null) {
      return map(repository);
    }
    return null;
  }

  private int computePageTotal(int totalHits, int pageSize) {
    if (totalHits % pageSize > 0) {
      return totalHits / pageSize + 1;
    } else {
      return totalHits / pageSize;
    }
  }

  protected abstract HitDto map(@Context QueryResult queryResult, Hit hit);

  @Data
  public static class EmbeddedRepositoryDto extends HalRepresentation {
    private String namespace;
    private String name;
    private String type;
    public EmbeddedRepositoryDto(Links links, Embedded embedded) {
      super(links, embedded);
    }
  }
}

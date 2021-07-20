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

import com.damnhandy.uri.template.UriTemplate;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import de.otto.edison.hal.paging.NumberedPaging;
import de.otto.edison.hal.paging.PagingRel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.search.Hit;
import sonia.scm.search.QueryResult;
import sonia.scm.web.EdisonHalAppender;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.damnhandy.uri.template.UriTemplate.fromTemplate;
import static de.otto.edison.hal.Links.linkingTo;
import static de.otto.edison.hal.paging.NumberedPaging.zeroBasedNumberedPaging;

@Mapper
public abstract class QueryResultMapper extends HalAppenderMapper {

  public abstract QueryResultDto map(@Context SearchParameters params, QueryResult result);

  @AfterMapping
  void setPageValues(@MappingTarget QueryResultDto dto, QueryResult result, @Context SearchParameters params) {
    int totalHits = (int) result.getTotalHits();
    dto.setPageTotal(computePageTotal(totalHits, params.getPageSize()));
    dto.setPage(params.getPage());
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

    applyEnrichers(new EdisonHalAppender(links, embedded), hit, queryResult);
    return new HitDto(links.build(), embedded.build());
  }

  private int computePageTotal(int totalHits, int pageSize) {
    if (totalHits % pageSize > 0) {
      return totalHits / pageSize + 1;
    } else {
      return totalHits / pageSize;
    }
  }

  protected abstract HitDto map(@Context QueryResult queryResult, Hit hit);
}

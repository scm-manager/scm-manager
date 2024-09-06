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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.RepositoryType;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RepositoryTypeCollectionToDtoMapperTest {

  private final URI baseUri = URI.create("https://scm-manager.org/scm/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private Subject subject;

  @InjectMocks
  private RepositoryTypeToRepositoryTypeDtoMapperImpl mapper;

  private final List<RepositoryType> types = Lists.newArrayList(
    new RepositoryType("hk", "Hitchhiker", Sets.newHashSet()),
    new RepositoryType("hog", "Heart of Gold", Sets.newHashSet())
  );

  private RepositoryTypeCollectionToDtoMapper collectionMapper;

  @Before
  public void setUpEnvironment() {
    collectionMapper = new RepositoryTypeCollectionToDtoMapper(mapper, resourceLinks);
    ThreadContext.bind(subject);
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldHaveEmbeddedDtos() {
    HalRepresentation mappedTypes = collectionMapper.map(types);
    Embedded embedded = mappedTypes.getEmbedded();
    List<RepositoryTypeDto> embeddedTypes = embedded.getItemsBy("repositoryTypes", RepositoryTypeDto.class);
    assertEquals("hk", embeddedTypes.get(0).getName());
    assertEquals("hog", embeddedTypes.get(1).getName());
  }

  @Test
  public void shouldHaveSelfLink() {
    HalRepresentation mappedTypes = collectionMapper.map(types);
    Optional<Link> self = mappedTypes.getLinks().getLinkBy("self");
    assertEquals("https://scm-manager.org/scm/v2/repositoryTypes/", self.get().getHref());
  }

}

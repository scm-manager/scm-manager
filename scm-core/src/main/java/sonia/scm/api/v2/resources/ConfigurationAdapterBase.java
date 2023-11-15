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

import com.github.sdorra.ssp.PermissionCheck;
import com.google.common.annotations.Beta;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 * This can be used as a base class for configuration resources.
 *
 * @param <DAO> The class of the data access object used to persist the configuration.
 * @param <DTO> The class of the data transfer objects used in the rest api.
 * @since 2.41.0
 */
@Beta
@Slf4j
public abstract class ConfigurationAdapterBase<DAO, DTO extends HalRepresentation> implements HalEnricher {

  protected final ConfigurationStoreFactory configurationStoreFactory;

  protected final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  private final Class<DAO> daoClass;
  private final DtoToDaoMapper<DTO, DAO> dtoToDaoMapper;
  private final DaoToDtoMapper<DAO, DTO> daoToDtoMapper;

  /**
   * Creates the resource. To do so, you have to provide the {@link ConfigurationStoreFactory} and
   * the {@link ScmPathInfoStore} as a {@link Provider} and implementations for
   * the DAO and the DTO.
   * <br>
   * The DAO class has to have a default constructor that creates the default (initial)
   * configuration.
   * <br>
   * The DTO class should be created with <code>@GenerateDto</code> annotation using Conveyor.
   * If the implementation is done manually, it has to provide two methods:
   * <ul>
   *   <li>A static method <code>DTO from(DAO, Links)</code> creating the DTO instance
   *     for the given DAO with the provided links.</li>
   *   <li>A method <code>DAO toEntity()</code> creating the DAO from the DTO.</li>
   * </ul>
   * If either one is missing, you will see {@link IllegalDaoClassException}s on your way.
   * <br>
   * The implementation may look like this:
   * <pre>
   *   @Path("/v2/test")
   *     private static class TestConfigurationAdapter extends ConfigurationAdapterBase<TestConfiguration, TestConfigurationDto> {
   *
   *     @Inject
   *     public TestConfigurationResource(ConfigurationStoreFactory configurationStoreFactory, Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
   *       super(configurationStoreFactory, scmPathInfoStoreProvider, TestConfiguration.class, TestConfigurationDto.class);
   *     }
   *
   *     @Override
   *     protected String getName() {
   *       return "testConfig";
   *     }
   *   }
   * </pre>
   *
   * @param configurationStoreFactory The configuration store factory provided from injection.
   * @param scmPathInfoStoreProvider The path info store provider provided from injection.
   * @param daoClass The DAO class instance.
   * @param dtoClass The DTO class instance.
   */
  @SuppressWarnings("unchecked")
  protected ConfigurationAdapterBase(ConfigurationStoreFactory configurationStoreFactory,
                                     Provider<ScmPathInfoStore> scmPathInfoStoreProvider,
                                     Class<DAO> daoClass,
                                     Class<DTO> dtoClass) {
    this.configurationStoreFactory = configurationStoreFactory;
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.daoClass = daoClass;
    this.dtoToDaoMapper = (DTO dto) -> {
      try {
        return (DAO) dtoClass.getDeclaredMethod("toEntity").invoke(dto);
      } catch (Exception e) {
        throw new IllegalDtoClassException(e);
      }
    };
    this.daoToDtoMapper = (DAO entity, Links.Builder linkBuilder) -> {
      try {
        return (DTO) dtoClass.getMethod("from", daoClass, Links.class)
          .invoke(null, entity, linkBuilder.build());
      } catch (Exception e) {
        throw new IllegalDtoClassException(e);
      }
    };
  }

  public DAO getConfiguration() {
    return getConfigStore().getOptional().orElse(createDefaultDaoInstance());
  }

  protected abstract String getName();

  protected String getStoreName() {
    return toKebap(getName());
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("")
  public DTO get(@Context UriInfo uriInfo) {
    PermissionCheck readPermission = getReadPermission();
    if (readPermission != null) {
      readPermission.check();
    }
    return daoToDtoMapper.mapDaoToDto(getConfiguration(), createDtoLinks());
  }

  private DAO createDefaultDaoInstance() {
    try {
      return daoClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalDaoClassException(e);
    }
  }

  private ConfigurationStore<DAO> getConfigStore() {
    return configurationStoreFactory.withType(daoClass).withName(toKebap(getName())).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  public void update(@NotNull @Valid DTO payload) {
    PermissionCheck writePermission = getWritePermission();
    if (writePermission != null) {
      writePermission.check();
    }
    getConfigStore().set(dtoToDaoMapper.mapDtoToDao(payload));
  }

  private Links.Builder createDtoLinks() {
    Links.Builder builder = Links.linkingTo();
    builder.single(Link.link("self", getReadLink()));
    PermissionCheck writePermission = getWritePermission();
    if (writePermission == null || writePermission.isPermitted()) {
      builder.single(Link.link("update", getUpdateLink()));
    }

    return builder;
  }

  private String getReadLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), this.getClass());
    return linkBuilder.method("get").parameters().href();
  }

  private String getUpdateLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), this.getClass());
    return linkBuilder.method("update").parameters().href();
  }

  @Override
  public final void enrich(HalEnricherContext context, HalAppender appender) {
    PermissionCheck readPermission = getReadPermission();
    if (readPermission == null || readPermission.isPermitted()) {
      appender.appendLink(getName(), getReadLink());
    }
  }

  @Nullable
  protected PermissionCheck getReadPermission() {
    return ConfigurationPermissions.read(getName());
  }

  @Nullable
  protected PermissionCheck getWritePermission() {
    return ConfigurationPermissions.write(getName());
  }

  private static class IllegalDtoClassException extends RuntimeException {
    public IllegalDtoClassException(Throwable cause) {
      super("Missing method #from(DAO, Links) or #toEntity() in DTO class; see JavaDoc for ConfigurationResourceBase", cause);
    }
  }

  private static class IllegalDaoClassException extends RuntimeException {
    public IllegalDaoClassException(Throwable cause) {
      super("Missing default constructor in DAO class; see JavaDoc for ConfigurationResourceBase", cause);
    }
  }

  private String toKebap(String other) {
    return other.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
  }

  private interface DaoToDtoMapper<DAO, DTO> {
    DTO mapDaoToDto(DAO entity, Links.Builder linkBuilder);
  }

  private interface DtoToDaoMapper<DTO, DAO> {
    DAO mapDtoToDao(DTO dto);
  }
}

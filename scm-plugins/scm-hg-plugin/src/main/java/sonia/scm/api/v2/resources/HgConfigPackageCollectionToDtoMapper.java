package sonia.scm.api.v2.resources;

import sonia.scm.installer.HgPackage;

import javax.inject.Inject;

public class HgConfigPackageCollectionToDtoMapper extends CollectionToDtoMapper<HgPackage, HgConfigPackageDto>  {

  private UriInfoStore uriInfoStore;

  @Inject
  public HgConfigPackageCollectionToDtoMapper(HgConfigPackageToDtoMapper mapper, UriInfoStore uriInfoStore) {
    super("packages", mapper);
    this.uriInfoStore = uriInfoStore;
  }

  @Override
  protected String createSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("get").parameters().href();
  }
}

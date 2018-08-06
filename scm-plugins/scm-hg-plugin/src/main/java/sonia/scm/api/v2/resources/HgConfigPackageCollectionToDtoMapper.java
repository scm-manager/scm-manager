package sonia.scm.api.v2.resources;

import sonia.scm.installer.HgPackage;

import javax.inject.Inject;

public class HgConfigPackageCollectionToDtoMapper extends CollectionToDtoMapper<HgPackage, HgConfigPackageDto>  {

  static final String COLLECTION_NAME = "packages";
  private UriInfoStore uriInfoStore;

  @Inject
  public HgConfigPackageCollectionToDtoMapper(HgConfigPackageToDtoMapper mapper, UriInfoStore uriInfoStore) {
    super(COLLECTION_NAME, mapper);
    this.uriInfoStore = uriInfoStore;
  }

  @Override
  protected String createSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(uriInfoStore.get(), HgConfigResource.class);
    return linkBuilder.method("getPackagesResource").parameters().href();
  }
}

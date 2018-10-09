package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.ReducedModelObject;

@Mapper
public abstract class ReducedObjectModelToDtoMapper {

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract ReducedObjectModelDto map(ReducedModelObject modelObject);

}

package sonia.scm.api.v2.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.slf4j.MDC;
import sonia.scm.ExceptionWithContext;

@Mapper
public abstract class ExceptionWithContextToErrorDtoMapper {

  @Mapping(target = "errorCode", source = "code")
  @Mapping(target = "transactionId", ignore = true)
  @Mapping(target = "violations", ignore = true)
  @Mapping(target = "url", ignore = true)
  public abstract ErrorDto map(ExceptionWithContext exception);

  @AfterMapping
  void setTransactionId(@MappingTarget ErrorDto dto) {
    dto.setTransactionId(MDC.get("transaction_id"));
  }
}

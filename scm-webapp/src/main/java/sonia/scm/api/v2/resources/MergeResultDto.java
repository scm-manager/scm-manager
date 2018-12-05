package sonia.scm.api.v2.resources;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class MergeResultDto {
  private Collection<String> filesWithConflict;
}

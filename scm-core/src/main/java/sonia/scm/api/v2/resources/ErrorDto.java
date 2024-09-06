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

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.ContextEntry;

import java.util.List;

@Getter @Setter
public class ErrorDto {
  private String transactionId;
  private String errorCode;
  private List<ContextEntry> context;
  private String message;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<AdditionalMessageDto> additionalMessages;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @XmlElementWrapper(name = "violations")
  private List<ConstraintViolationDto> violations;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String url;

  @XmlRootElement(name = "violation")
  @Getter @Setter
  public static class ConstraintViolationDto {
    private String path;
    private String message;
  }

  @Getter @Setter
  public static class AdditionalMessageDto {
    private String key;
    private String message;
  }
}

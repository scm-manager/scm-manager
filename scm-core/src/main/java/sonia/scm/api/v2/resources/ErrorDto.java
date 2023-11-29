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

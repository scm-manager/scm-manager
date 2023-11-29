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
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.util.ValidationUtil;

import java.time.Instant;

@NoArgsConstructor @Getter @Setter
public class UserDto extends HalRepresentation implements CreateUserDto, UpdateUserDto {
  private boolean active;
  private boolean external;
  private Instant creationDate;
  @NotEmpty
  private String displayName;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Instant lastModified;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Email
  private String mail;
  @Pattern(regexp = ValidationUtil.REGEX_NAME)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String password;
  private String type;

  UserDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}

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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter @Setter @NoArgsConstructor
public class BranchDto extends HalRepresentation {

  private static final String VALID_CHARACTERS_AT_START_AND_END = "\\w-,;\\]{}@&+=$#`|<>";
  private static final String VALID_CHARACTERS = VALID_CHARACTERS_AT_START_AND_END + "/.";
  static final String VALID_BRANCH_NAMES = "[" + VALID_CHARACTERS_AT_START_AND_END + "]([" + VALID_CHARACTERS + "]*[" + VALID_CHARACTERS_AT_START_AND_END + "])?";

  @NotEmpty @Length(min = 1, max=100) @Pattern(regexp = VALID_BRANCH_NAMES)
  private String name;
  private String revision;
  private boolean defaultBranch;

  BranchDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}

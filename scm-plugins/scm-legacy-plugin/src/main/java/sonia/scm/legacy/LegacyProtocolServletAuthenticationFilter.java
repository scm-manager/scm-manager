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

package sonia.scm.legacy;

import jakarta.inject.Inject;
import sonia.scm.Priority;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.Filters;
import sonia.scm.filter.WebElement;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.WebTokenGenerator;
import sonia.scm.web.filter.HttpProtocolServletAuthenticationFilterBase;

import java.util.Set;

@Priority(Filters.PRIORITY_AUTHENTICATION)
@WebElement(value = "/git/*", morePatterns = {"/hg/*", "/svn/*"})
public class LegacyProtocolServletAuthenticationFilter extends HttpProtocolServletAuthenticationFilterBase {

  @Inject
  public LegacyProtocolServletAuthenticationFilter(ScmConfiguration configuration, Set<WebTokenGenerator> tokenGenerators, UserAgentParser userAgentParser) {
    super(configuration, tokenGenerators, userAgentParser);
  }
}

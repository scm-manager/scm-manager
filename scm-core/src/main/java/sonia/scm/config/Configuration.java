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
    
package sonia.scm.config;

import com.github.sdorra.ssp.PermissionObject;
import com.github.sdorra.ssp.StaticPermissions;

/**
 * Base for all kinds of configurations.
 *
 * Allows for permission like
 *
 * <ul>
 *   <li>"configuration:read:global",</li>
 *   <li>"configuration:write:svn",</li>
 *   <li>"configuration:*:git",</li>
 *   <li>"configuration:*"</li>
 * </ul>
 *
 * <br/>
 *
 * And for permission checks like {@code ConfigurationPermissions.read(configurationObject).check();}
 */
@StaticPermissions(
  value = "configuration",
  permissions = {"read", "write"},
  globalPermissions = {"list"},
  custom = true, customGlobal = true
)
public interface Configuration extends PermissionObject {
}

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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import ProtocolInformation from "./ProtocolInformation";
import SvnAvatar from "./SvnAvatar";
import SvnGlobalConfiguration from "./SvnGlobalConfiguration";

const svnPredicate = (props: any) => {
  return props.repository && props.repository.type === "svn";
};

binder.bind<extensionPoints.RepositoryDetailsInformation>(
  "repos.repository-details.information",
  ProtocolInformation,
  svnPredicate
);
binder.bind("repos.repository-avatar", SvnAvatar, svnPredicate);

// bind global configuration

cfgBinder.bindGlobal("/svn", "scm-svn-plugin.config.link", "svnConfig", SvnGlobalConfiguration);

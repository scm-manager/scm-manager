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
import ProtocolInformation from "./ProtocolInformation";
import HgAvatar from "./HgAvatar";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import HgGlobalConfiguration from "./HgGlobalConfiguration";
import HgBranchInformation from "./HgBranchInformation";
import HgTagInformation from "./HgTagInformation";
import HgRepositoryConfigurationForm from "./HgRepositoryConfigurationForm";

const hgPredicate = (props: any) => {
  return props.repository && props.repository.type === "hg";
};

binder.bind("repos.repository-details.information", ProtocolInformation, hgPredicate);
binder.bind<extensionPoints.ReposBranchDetailsInformationExtension>(
  "repos.branch-details.information",
  HgBranchInformation,
 { priority: 100, predicate: hgPredicate
});
binder.bind("repos.tag-details.information", HgTagInformation, hgPredicate);
binder.bind("repos.repository-avatar", HgAvatar, hgPredicate);

// bind repository specific configuration

binder.bind<extensionPoints.RepoConfigRouteExtension>("repo-config.route", HgRepositoryConfigurationForm, hgPredicate);

// bind global configuration

cfgBinder.bindGlobal("/hg", "scm-hg-plugin.config.link", "hgConfig", HgGlobalConfiguration);

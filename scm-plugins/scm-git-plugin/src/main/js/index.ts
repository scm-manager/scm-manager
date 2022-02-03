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
import GitAvatar from "./GitAvatar";

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GitGlobalConfiguration from "./GitGlobalConfiguration";
import GitBranchInformation from "./GitBranchInformation";
import GitMergeInformation from "./GitMergeInformation";
import RepositoryConfig from "./RepositoryConfig";
import GitTagInformation from "./GitTagInformation";

// repository

// @visibleForTesting
export const gitPredicate = (props: any) => {
  return !!(props && props.repository && props.repository.type === "git");
};

binder.bind<extensionPoints.RepositoryDetailsInformation>(
  "repos.repository-details.information",
  ProtocolInformation,
  gitPredicate
);
binder.bind("repos.branch-details.information", GitBranchInformation, { priority: 100, predicate: gitPredicate });

binder.bind<extensionPoints.RepositoryTagDetailsInformation>(
  "repos.tag-details.information",
  GitTagInformation,
  gitPredicate
);
binder.bind("repos.repository-merge.information", GitMergeInformation, gitPredicate);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

binder.bind("repo-config.route", RepositoryConfig, gitPredicate);

// global config
cfgBinder.bindGlobal("/git", "scm-git-plugin.config.link", "gitConfig", GitGlobalConfiguration);

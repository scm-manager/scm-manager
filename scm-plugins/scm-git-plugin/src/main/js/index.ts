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
export const gitPredicate = (props: extensionPoints.RepositoryDetailsInformation["props"]) => {
  return !!(props && props.repository && props.repository.type === "git");
};

binder.bind<extensionPoints.RepositoryDetailsInformation>("repos.repository-details.information", ProtocolInformation, {
  predicate: gitPredicate,
  priority: 100,
});
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

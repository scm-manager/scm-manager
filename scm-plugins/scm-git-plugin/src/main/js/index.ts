import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import GitAvatar from "./GitAvatar";

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GitGlobalConfiguration from "./GitGlobalConfiguration";
import GitBranchInformation from "./GitBranchInformation";
import GitMergeInformation from "./GitMergeInformation";
import RepositoryConfig from "./RepositoryConfig";

// repository

// @visibleForTesting
export const gitPredicate = (props: any) => {
  return !!(props && props.repository && props.repository.type === "git");
};

binder.bind("repos.repository-details.information", ProtocolInformation, gitPredicate);
binder.bind("repos.branch-details.information", GitBranchInformation, gitPredicate);
binder.bind("repos.repository-merge.information", GitMergeInformation, gitPredicate);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

binder.bind("repo-config.route", RepositoryConfig, gitPredicate);

// global config
cfgBinder.bindGlobal("/git", "scm-git-plugin.config.link", "gitConfig", GitGlobalConfiguration);

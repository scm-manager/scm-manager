//@flow
import React from "react";
import {binder} from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import GitAvatar from "./GitAvatar";

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import GitGlobalConfiguration from "./GitGlobalConfiguration";
import GitMergeInformation from "./GitMergeInformation";
import RepositoryConfig from "./RepositoryConfig";

// repository

const gitPredicate = (props: Object) => {
  return props.repository && props.repository.type === "git";
};

binder.bind(
  "repos.repository-details.information",
  ProtocolInformation,
  gitPredicate
);
binder.bind(
  "repos.repository-merge.information",
  GitMergeInformation,
  gitPredicate
);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

cfgBinder.bindRepository(
  "/settings/configuration",
  "scm-git-plugin.repo-config.link",
  "configuration",
  RepositoryConfig
);
// global config

cfgBinder.bindGlobal(
  "/git",
  "scm-git-plugin.config.link",
  "gitConfig",
  GitGlobalConfiguration
);

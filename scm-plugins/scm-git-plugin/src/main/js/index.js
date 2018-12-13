//@flow
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import GitAvatar from "./GitAvatar";

import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GitGlobalConfiguration from "./GitGlobalConfiguration";
import GitMergeInformation from "./GitMergeInformation";

// repository

const gitPredicate = (props: Object) => {
  return props.repository && props.repository.type === "git";
};

binder.bind("repos.repository-details.information", ProtocolInformation, gitPredicate);
binder.bind("repos.repository-merge.information", GitMergeInformation, gitPredicate);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

// global config

cfgBinder.bindGlobal("/git", "scm-git-plugin.config.link", "gitConfig", GitGlobalConfiguration);

// @flow
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import HgAvatar from "./HgAvatar";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import HgGlobalConfiguration from "./HgGlobalConfiguration";
import HgBranchInformation from "./HgBranchInformation";

const hgPredicate = (props: Object) => {
  return props.repository && props.repository.type === "hg";
};

binder.bind(
  "repos.repository-details.information",
  ProtocolInformation,
  hgPredicate
);
binder.bind(
  "repos.branch-details.information",
  HgBranchInformation,
  hgPredicate
);
binder.bind("repos.repository-avatar", HgAvatar, hgPredicate);

// bind global configuration

cfgBinder.bindGlobal(
  "/hg",
  "scm-hg-plugin.config.link",
  "hgConfig",
  HgGlobalConfiguration
);

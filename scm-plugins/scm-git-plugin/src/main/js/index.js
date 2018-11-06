//@flow
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import GitAvatar from "./GitAvatar";
import GitConfigurationNavLink from "./GitConfigurationNavLink";
import GitConfigurationRoute from "./GitConfigurationRoute";

// repository

const gitPredicate = (props: Object) => {
  return props.repository && props.repository.type === "git";
};

binder.bind("repos.repository-details.information", ProtocolInformation, gitPredicate);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

// global config

const gitConfigPredicate = (props: Object) => {
  return props.links && props.links["gitConfig"];
};

binder.bind("config.navigation", GitConfigurationNavLink, gitConfigPredicate);
binder.bind("config.route", GitConfigurationRoute, gitConfigPredicate);

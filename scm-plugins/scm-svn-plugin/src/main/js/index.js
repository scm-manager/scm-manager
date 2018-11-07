// @flow
import { binder } from "@scm-manager/ui-extensions";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import ProtocolInformation from "./ProtocolInformation";
import SvnAvatar from "./SvnAvatar";
import SvnGlobalConfiguration from "./SvnGlobalConfiguration";

const svnPredicate = (props: Object) => {
  return props.repository && props.repository.type === "svn";
};

binder.bind("repos.repository-details.information", ProtocolInformation, svnPredicate);
binder.bind("repos.repository-avatar", SvnAvatar, svnPredicate);

// bind global configuration

cfgBinder.bindGlobal("/svn", "scm-svn-plugin.config.link", "svnConfig", SvnGlobalConfiguration);

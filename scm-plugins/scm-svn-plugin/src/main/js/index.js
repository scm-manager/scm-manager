// @flow
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import SvnAvatar from "./SvnAvatar";

const svnPredicate = (props: Object) => {
  return props.repository && props.repository.type === "svn";
};

binder.bind("repos.repository-details.information", ProtocolInformation, svnPredicate);
binder.bind("repos.repository-avatar", SvnAvatar, svnPredicate);

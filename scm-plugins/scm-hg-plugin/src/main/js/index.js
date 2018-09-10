// @flow
import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import HgAvatar from "./HgAvatar";

const hgPredicate = (props: Object) => {
  return props.repository && props.repository.type === "hg";
};

binder.bind("repos.repository-details.information", ProtocolInformation, hgPredicate);
binder.bind("repos.repository-avatar", HgAvatar, hgPredicate);

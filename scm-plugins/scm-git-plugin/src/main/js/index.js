import { binder } from "@scm-manager/ui-extensions";
import ProtocolInformation from './ProtocolInformation';
import GitAvatar from './GitAvatar';

const gitPredicate = (props: Object) => {
  return props.repository && props.repository.type === "git";
};

binder.bind("repos.repository-details.information", ProtocolInformation, gitPredicate);
binder.bind("repos.repository-avatar", GitAvatar, gitPredicate);

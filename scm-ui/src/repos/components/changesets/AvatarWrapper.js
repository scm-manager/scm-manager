//@flow
import * as React from "react";
import { binder } from "@scm-manager/ui-extensions";

type Props = {
  children: React.Node
};

class AvatarWrapper extends React.Component<Props> {
  render() {
    if (binder.hasExtension("changeset.avatar-factory")) {
      return <>{this.props.children}</>;
    }
    return null;
  }
}

export default AvatarWrapper;

import React, { Component, ReactNode } from "react";
import { binder } from "@scm-manager/ui-extensions";
import { EXTENSION_POINT } from "./Avatar";

type Props = {
  children: ReactNode;
};

class AvatarWrapper extends Component<Props> {
  render() {
    if (binder.hasExtension(EXTENSION_POINT)) {
      return <>{this.props.children}</>;
    }
    return null;
  }
}

export default AvatarWrapper;

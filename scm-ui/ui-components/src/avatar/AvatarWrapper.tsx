import React, { FC } from "react";
import { useBinder } from "@scm-manager/ui-extensions";
import { EXTENSION_POINT } from "./Avatar";

const AvatarWrapper: FC = ({ children }) => {
  const binder = useBinder();
  if (binder.hasExtension(EXTENSION_POINT)) {
    return <>{children}</>;
  }
  return null;
};

export default AvatarWrapper;

import * as React from "react";
import { Binder } from "./binder";
import { FC, ReactNode } from "react";
import useBinder from "./useBinder";

type Props = {
  name: string;
  renderAll?: boolean;
  props?: object;
};

const renderAllExtensions = (binder: Binder, name: string, props?: object) => {
  const extensions = binder.getExtensions(name, props);
  return (
    <>
      {extensions.map((Component, index) => {
        return <Component key={index} {...props} />;
      })}
    </>
  );
};

const renderSingleExtension = (binder: Binder, name: string, props?: object) => {
  const Component = binder.getExtension(name, props);
  if (!Component) {
    return null;
  }
  return <Component {...props} />;
};

const renderDefault = (children: ReactNode) => {
  if (children) {
    return <>{children}</>;
  }
  return null;
};

/**
 * ExtensionPoint renders components which are bound to an extension point.
 */
const ExtensionPoint: FC<Props> = ({ name, renderAll, props, children }) => {
  const binder = useBinder();
  if (!binder.hasExtension(name, props)) {
    return renderDefault(children);
  } else if (renderAll) {
    return renderAllExtensions(binder, name, props);
  }
  return renderSingleExtension(binder, name, props);
};

export default ExtensionPoint;

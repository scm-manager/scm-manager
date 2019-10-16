//@flow
import * as React from "react";
import binder from "./binder";

type Props = {
  name: string,
  renderAll?: boolean,
  props?: Object,
  children?: React.Node
};

/**
 * ExtensionPoint renders components which are bound to an extension point.
 */
class ExtensionPoint extends React.Component<Props> {
  renderAll(name: string, props?: Object) {
    const extensions = binder.getExtensions(name, props);
    return (
      <>
        {extensions.map((Component, index) => {
          return <Component key={index} {...props} />;
        })}
      </>
    );
  }

  renderSingle(name: string, props?: Object) {
    const Component = binder.getExtension(name, props);
    if (!Component) {
      return null;
    }
    return <Component {...props} />;
  }

  renderDefault() {
    const { children } = this.props;
    if (children) {
      return <>{children}</>;
    }
    return null;
  }

  render() {
    const { name, renderAll, props } = this.props;
    if (!binder.hasExtension(name, props)) {
      return this.renderDefault();
    } else if (renderAll) {
      return this.renderAll(name, props);
    }
    return this.renderSingle(name, props);
  }
}

export default ExtensionPoint;

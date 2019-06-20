// @flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  className?: string,
  children: React.Node
};

class ButtonAddons extends React.Component<Props> {
  render() {
    const { className, children } = this.props;

    var childWrapper = [];
    React.Children.forEach(children, child => {
      if (child) {
        childWrapper.push(<p className="control">{child}</p>);
      }
    });

    return (
      <div className={classNames("field", "is-addons", className)}>
        {childWrapper}
      </div>
    );
  }
}

export default ButtonAddons;

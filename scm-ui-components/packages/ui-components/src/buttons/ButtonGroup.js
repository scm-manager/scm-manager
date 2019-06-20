// @flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  connected?: boolean,
  addons?: boolean,
  className?: string,
  children: React.Node
};

class ButtonGroup extends React.Component<Props> {

  static defaultProps = {
    addons: true
  };

  render() {
    const {connected, addons, className, children} = this.props;

    if (!connected) {
      var childWrapper = [];
      React.Children.forEach(children, child => {
        if (child) {
          childWrapper.push(<p className="control">{child}</p>);
        }
      });

      return (
        <div className={classNames("field", "is-grouped", className)}>
          {childWrapper}
        </div>
      );
    }

    return (
      <div className={classNames("buttons", addons ? "has-addons" : "", className)}>
        {children}
      </div>
    );
  }
}

export default ButtonGroup;

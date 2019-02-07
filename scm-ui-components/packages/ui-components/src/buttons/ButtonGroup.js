// @flow
import * as React from "react";

type Props = {
  addons?: boolean,
  className?: string,
  children: React.Node
};

class ButtonGroup extends React.Component<Props> {

  static defaultProps = {
    addons: true
  };

  render() {
    const { addons, className, children } = this.props;
    let styleClasses = "buttons";
    if (addons) {
      styleClasses += " has-addons";
    }
    if (className) {
      styleClasses += " " + className;
    }
    return (
      <div className={styleClasses}>
        { children }
      </div>
    );
  }
}

export default ButtonGroup;

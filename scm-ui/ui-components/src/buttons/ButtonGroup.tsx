import React, { ReactNode } from "react";
import classNames from "classnames";

type Props = {
  className?: string;
  children: ReactNode;
};

class ButtonGroup extends React.Component<Props> {
  render() {
    const { className, children } = this.props;

    const childWrapper: ReactNode[] = [];
    React.Children.forEach(children, child => {
      if (child) {
        childWrapper.push(
          <div className="control" key={childWrapper.length}>
            {child}
          </div>
        );
      }
    });

    return (
      <div className={classNames("field", "is-grouped", className)}>
        {childWrapper}
      </div>
    );
  }
}

export default ButtonGroup;

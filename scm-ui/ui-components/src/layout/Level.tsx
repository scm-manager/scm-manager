import React, { ReactNode } from "react";
import classNames from "classnames";

type Props = {
  className?: string;
  left?: ReactNode;
  children?: ReactNode;
  right?: ReactNode;
};

export default class Level extends React.Component<Props> {
  render() {
    const { className, left, children, right } = this.props;
    let child = null;
    if (children) {
      child = <div className="level-item">{children}</div>;
    }

    return (
      <div className={classNames("level", className)}>
        <div className="level-left">{left}</div>
        {child}
        <div className="level-right">{right}</div>
      </div>
    );
  }
}

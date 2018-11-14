//@flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  message: string,
  className: string,
  children: React.Node
};

class Tooltip extends React.Component<Props> {
  render() {
    const { className, message, children } = this.props;
    const multiline = message.length > 60 ? "is-tooltip-multiline" : "";
    return (
      <div
        className={classNames("tooltip", "is-tooltip-right", multiline, className)}
        data-tooltip={message}
      >
        {children}
      </div>
    );
  }
}

export default Tooltip;

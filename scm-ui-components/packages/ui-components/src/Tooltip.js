//@flow
import * as React from "react";
import classNames from "classnames";

type Props = {
  message: string,
  className: string,
  location?: string,
  children: React.Node
};

class Tooltip extends React.Component<Props> {

  static defaultProps = {
    location: "right"
  };

  render() {
    const { className, message, location, children } = this.props;
    const multiline = message.length > 60 ? "is-tooltip-multiline" : "";
    return (
      <span
        className={classNames("tooltip", "is-tooltip-" + location, multiline, className)}
        data-tooltip={message}
      >
        {children}
      </span>
    );
  }
}

export default Tooltip;

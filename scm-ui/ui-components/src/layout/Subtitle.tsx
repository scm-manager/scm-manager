import React from "react";
import classNames from "classnames";

type Props = {
  subtitle?: string;
  className?: string;
};

class Subtitle extends React.Component<Props> {
  render() {
    const { subtitle, className } = this.props;
    if (subtitle) {
      return <h2 className={classNames("subtitle", className)}>{subtitle}</h2>;
    }
    return null;
  }
}

export default Subtitle;

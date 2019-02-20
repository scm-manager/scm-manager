// @flow
import React from "react";

type Props = {
  subtitle?: string
};

class Subtitle extends React.Component<Props> {
  render() {
    const { subtitle } = this.props;
    if (subtitle) {
      return <h2 className="subtitle">{subtitle}</h2>;
    }
    return null;
  }
}

export default Subtitle;

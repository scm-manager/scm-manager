// @flow
import React from "react";

type Props = {
  subtitle?: string
};

class Subtitle extends React.Component<Props> {
  render() {
    const { subtitle } = this.props;
    if (subtitle) {
      return <h1 className="subtitle">{subtitle}</h1>;
    }
    return null;
  }
}

export default Subtitle;

// @flow
import React from "react";

type Props = {
  address?: string
};

class MailLink extends React.Component<Props> {
  render() {
    const { address } = this.props;
    if (!address) {
      return null;
    }
    return <a href={"mailto: " + address}>{address}</a>;
  }
}

export default MailLink;

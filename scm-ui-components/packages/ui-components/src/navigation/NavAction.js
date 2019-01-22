//@flow
import React from "react";

type Props = {
  label: string,
  action: () => void
};

class NavAction extends React.Component<Props> {
  render() {
    const { label, action } = this.props;
    return (
      <li>
        <a onClick={action} role="button" tabIndex="0">{label}</a>
      </li>
    );
  }
}

export default NavAction;

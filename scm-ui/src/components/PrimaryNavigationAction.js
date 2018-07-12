//@flow
import * as React from "react";

type Props = {
  label: string,
  onClick: () => void
};

class PrimaryNavigationAction extends React.Component<Props> {
  render() {
    const { label, onClick } = this.props;
    return (
      <li>
        <a onClick={onClick}>{label}</a>
      </li>
    );
  }
}

export default PrimaryNavigationAction;

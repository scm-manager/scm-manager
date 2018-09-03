//@flow
import * as React from "react";

type Props = {
  children?: React.Node
};

class Navigation extends React.Component<Props> {
  render() {
    return <aside className="menu">{this.props.children}</aside>;
  }
}

export default Navigation;

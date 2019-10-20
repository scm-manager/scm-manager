import React, { ReactNode } from "react";

type Props = {
  children?: ReactNode;
};

class Navigation extends React.Component<Props> {
  render() {
    return <aside className="menu">{this.props.children}</aside>;
  }
}

export default Navigation;

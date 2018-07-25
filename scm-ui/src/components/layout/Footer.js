//@flow
import React from "react";
import type { Me } from "../../types/Me";

type Props = {
  me?: Me
};

class Footer extends React.Component<Props> {
  render() {
    const { me } = this.props;
    if (!me) {
      return "";
    }
    return (
      <footer className="footer">
        <div className="container is-centered">
          <p className="has-text-centered">{me.displayName}</p>
        </div>
      </footer>
    );
  }
}

export default Footer;

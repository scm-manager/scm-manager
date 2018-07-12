//@flow
import React from "react";
import type { Me } from "../types/me";

type Props = {
  me: Me
};

class Footer extends React.Component<Props> {
  render() {
    return (
      <footer className="footer">
        <div className="container is-centered">
          <p className="has-text-centered">{this.props.me.username}</p>
        </div>
      </footer>
    );
  }
}

export default Footer;

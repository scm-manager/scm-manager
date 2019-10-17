//@flow
import React from "react";
import type { Me } from "@scm-manager/ui-types";
import {Link} from "react-router-dom";

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
          <p className="has-text-centered"><Link to={"/me"}>{me.displayName}</Link></p>
        </div>
      </footer>
    );
  }
}

export default Footer;

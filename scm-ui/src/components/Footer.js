//@flow
import React from "react";

type Props = {
  me?: string
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
          <p className="has-text-centered">{me}</p>
        </div>
      </footer>
    );
  }
}

export default Footer;

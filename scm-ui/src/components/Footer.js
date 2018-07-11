//@flow
import React from "react";

type Props = {
  me: any
};

class Footer extends React.Component<Props> {
  render() {
    return (
      <footer class="footer">
        <div class="container is-centered">
          <p class="has-text-centered">{this.props.me.username}</p>
        </div>
      </footer>
    );
  }
}

export default Footer;

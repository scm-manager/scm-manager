//@flow
import * as React from "react";
import Logo from "./Logo";

type Props = {
  children?: React.Node
};

class Header extends React.Component<Props> {
  render() {
    const { children } = this.props;
    return (
      <section className="hero is-dark is-small">
        <div className="hero-body">
          <div className="container">
            <div className="columns is-vcentered">
              <div className="column">
                <Logo />
              </div>
            </div>
          </div>
        </div>
        <div className="hero-foot">
          <div className="container">{children}</div>
        </div>
      </section>
    );
  }
}

export default Header;

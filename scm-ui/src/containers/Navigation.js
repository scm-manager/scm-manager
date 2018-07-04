//@flow
import React from 'react';
import {Link} from 'react-router-dom';

type Props = {};

type State = {
  collapsed: boolean
};

class Navigation extends React.Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: true
    };
  }

  toggleCollapse = () => {
    this.setState({
      collapsed: !this.state.collapsed
    });
  };

  render() {

    return (
      <nav className="navbar navbar-default navbar-fixed-top">
        <div className="container">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#navbar" aria-expanded="false" aria-controls="navbar"
                    onClick={this.toggleCollapse}>
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>

            <Link className="navbar-brand" to="/">
              SCM 2 Test UI
            </Link>
          </div>
        </div>
      </nav>
    );
  }

}

export default Navigation;

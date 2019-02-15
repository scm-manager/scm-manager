// @flow
import React from "react";
import { withRouter } from "react-router-dom";


type Props = {
  location: any,
  children: any
}

class ScrollToTop extends React.Component<Props> {
  componentDidUpdate(prevProps) {
    if (this.props.location.pathname !== prevProps.location.pathname) {
      window.scrollTo(0, 0);
    }
  }

  render() {
    return this.props.children;
  }
}

export default withRouter(ScrollToTop);

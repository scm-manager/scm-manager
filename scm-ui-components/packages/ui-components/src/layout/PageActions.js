//@flow
import * as React from "react";
import Loading from "./../Loading";

type Props = {
  loading?: boolean,
  error?: Error,
  children: React.Node
};

class PageActions extends React.Component<Props> {
  render() {
    return <>{this.renderContent()}</>;
  }

  renderContent() {
    const { loading, children, error } = this.props;
    if (error) {
      return null;
    }
    if (loading) {
      return <Loading />;
    }
    return children;
  }
}

export default PageActions;

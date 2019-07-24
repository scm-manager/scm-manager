//@flow
import * as React from "react";
import Loading from "./../Loading";

type Props = {
  loading?: boolean,
  error?: Error,
  children: React.Node
};

export default class PageActions extends React.Component<Props> {
  displayName: string = "PageActions";

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

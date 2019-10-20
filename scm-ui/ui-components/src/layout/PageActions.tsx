import React, { ReactNode } from "react";
import Loading from "./../Loading";

type Props = {
  loading?: boolean;
  error?: Error;
  children: ReactNode;
};

export default class PageActions extends React.Component<Props> {
  static displayName = "PageActions";

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

//@flow
import * as React from "react";
import Loading from "./Loading";
import ErrorNotification from "./ErrorNotification";

type Props = {
  title: string,
  subtitle?: string,
  loading?: boolean,
  error?: Error,
  children: React.Node
};

class Page extends React.Component<Props> {
  render() {
    const { title, error } = this.props;
    return (
      <section className="section">
        <div className="container">
          <h1 className="title">{title}</h1>
          {this.renderSubtitle()}
          <ErrorNotification error={error} />
          {this.renderContent()}
        </div>
      </section>
    );
  }

  renderSubtitle() {
    const { subtitle } = this.props;
    if (subtitle) {
      return <h2 className="subtitle">{subtitle}</h2>;
    }
    return null;
  }

  renderContent() {
    const { loading, children } = this.props;
    if (loading) {
      return <Loading />;
    }
    return children;
  }
}

export default Page;

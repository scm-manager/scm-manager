//@flow
import * as React from "react";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import Title from "./Title";
import Subtitle from "./Subtitle";

type Props = {
  title?: string,
  subtitle?: string,
  loading?: boolean,
  error?: Error,
  showContentOnError?: boolean,
  children: React.Node
};

class Page extends React.Component<Props> {
  render() {
    const { title, error, subtitle } = this.props;
    return (
      <section className="section">
        <div className="container">
          <Title title={title} />
          <Subtitle subtitle={subtitle} />
          <ErrorNotification error={error} />
          {this.renderContent()}
        </div>
      </section>
    );
  }

  renderContent() {
    const { loading, children, showContentOnError, error } = this.props;
    if (error && !showContentOnError) {
      return null;
    }
    if (loading) {
      return <Loading />;
    }
    return children;
  }
}

export default Page;

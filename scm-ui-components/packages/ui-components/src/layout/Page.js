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
          <div className="columns">
            <div className="column">
              <Title title={title} />
              <Subtitle subtitle={subtitle} />
            </div>
            <div className="column is-two-fifths is-pulled-right">
              {this.renderPageActions()}
            </div>
          </div>
          <ErrorNotification error={error} />
          {this.renderContent()}
        </div>
      </section>
    );
  }

  renderPageActions() {
    const { children } = this.props;

    let content = null;
    React.Children.forEach(children, child => {
      if (child && child.type.name === "PageActions") {
        content = child;
      }
    });
    return content;
  }

  renderContent() {
    const { loading, children, showContentOnError, error } = this.props;

    if (error && !showContentOnError) {
      return null;
    }
    if (loading) {
      return <Loading />;
    }

    let content = [];
    React.Children.forEach(children, child => {
      if (child && child.type.name !== "PageActions") {
        content.push(child);
      }
    });
    return content;
  }
}

export default Page;

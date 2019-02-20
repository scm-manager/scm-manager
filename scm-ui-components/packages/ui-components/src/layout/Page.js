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
    const { error } = this.props;
    return (
      <section className="section">
        <div className="container">
          {this.renderPageHeader()}
          <ErrorNotification error={error} />
          {this.renderContent()}
        </div>
      </section>
    );
  }

  renderPageHeader() {
    const { title, subtitle, children } = this.props;

    let content = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && child.type.name === "PageActions") {
        content = child;
        pageActionsExists = true;
      }
    });

    return (
      <div
        className={
          pageActionsExists ? "columns page-header-with-actions" : "columns"
        }
      >
        <div className="column">
          <Title title={title} />
          <Subtitle subtitle={subtitle} />
        </div>
        <div className="column is-two-fifths">
          <div className="is-pulled-right">{content}</div>
        </div>
      </div>
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

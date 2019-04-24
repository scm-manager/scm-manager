//@flow
import * as React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import Title from "./Title";
import Subtitle from "./Subtitle";
import PageActions from "./PageActions";
import ErrorBoundary from "../ErrorBoundary";

type Props = {
  title?: string,
  subtitle?: string,
  loading?: boolean,
  error?: Error,
  showContentOnError?: boolean,
  children: React.Node,

  // context props
  classes: Object
};

const styles = {
  actions: {
    display: "flex",
    justifyContent: "flex-end"
  }
};

class Page extends React.Component<Props> {
  render() {
    const { error } = this.props;
    return (
      <section className="section">
        <div className="container">
          {this.renderPageHeader()}
          <ErrorBoundary>
            <ErrorNotification error={error} />
            {this.renderContent()}
          </ErrorBoundary>
        </div>
      </section>
    );
  }

  renderPageHeader() {
    const { error, title, subtitle, children, classes } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && !error) {
        if (child.type.name === PageActions.name)
          pageActions = (
            <div
              className={classNames(
                classes.actions,
                "column is-three-fifths is-mobile-action-spacing"
              )}
            >
              {child}
            </div>
          );
        pageActionsExists = true;
      }
    });
    let underline = pageActionsExists ? (
      <hr className="header-with-actions" />
    ) : null;

    return (
      <>
        <div className="columns">
          <div className="column">
            <Title title={title} />
            <Subtitle subtitle={subtitle} />
          </div>
          {pageActions}
        </div>
        {underline}
      </>
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
      if (child) {
        if (child.type.name !== PageActions.name) {
          content.push(child);
        }
      }
    });
    return content;
  }
}

export default injectSheet(styles)(Page);

//@flow
import * as React from "react";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import Title from "./Title";
import Subtitle from "./Subtitle";
import injectSheet from "react-jss";
import classNames from "classnames";
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
  spacing: {
    marginTop: "1.25rem",
    textAlign: "right"
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
            <ErrorNotification error={error}/>
            {this.renderContent()}
          </ErrorBoundary>
        </div>
      </section>

    );
  }

  renderPageHeader() {
    const { title, subtitle, children, classes } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && child.type.name === PageActions.name) {
        pageActions = (
          <div className="column is-two-fifths">
            <div
              className={classNames(
                classes.spacing,
                "is-mobile-create-button-spacing"
              )}
            >
              {child}
            </div>
          </div>
        );
        pageActionsExists = true;
      }
    });
    let underline = pageActionsExists ? (
      <hr className="header-with-actions"/>
    ) : null;

    return (
      <>
        <div className="columns">
          <div className="column">
            <Title title={title}/>
            <Subtitle subtitle={subtitle}/>
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
      return <Loading/>;
    }

    let content = [];
    React.Children.forEach(children, child => {
      if (child && child.type.name !== PageActions.name) {
        content.push(child);
      }
    });
    return content;
  }
}

export default injectSheet(styles)(Page);

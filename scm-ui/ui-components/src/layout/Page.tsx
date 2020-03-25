import React, { ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import Title from "./Title";
import Subtitle from "./Subtitle";
import PageActions from "./PageActions";
import ErrorBoundary from "../ErrorBoundary";

type Props = {
  title?: string;
  afterTitle?: ReactNode;
  subtitle?: string;
  loading?: boolean;
  error?: Error;
  showContentOnError?: boolean;
  children: ReactNode;
};

const PageActionContainer = styled.div`
  justify-content: flex-end;
  align-items: center;

  // every child except first
  > * ~ * {
    margin-left: 1.25rem;
  }
`;

const MarginLeft = styled.div`
margin-left: 0.5rem;
`;

export default class Page extends React.Component<Props> {
  componentDidUpdate() {
    const { title } = this.props;
    if (title && title !== document.title) {
      document.title = title;
    }
  }

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

  isPageAction(node: any) {
    return (
      node.displayName === PageActions.displayName || (node.type && node.type.displayName === PageActions.displayName)
    );
  }

  renderPageHeader() {
    const { error, title, afterTitle, subtitle, children } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && !error) {
        if (this.isPageAction(child)) {
          pageActions = (
            <PageActionContainer
              className={classNames("column", "is-three-fifths", "is-mobile-action-spacing", "is-flex")}
            >
              {child}
            </PageActionContainer>
          );
          pageActionsExists = true;
        }
      }
    });
    const underline = pageActionsExists ? <hr className="header-with-actions" /> : null;

    return (
      <>
        <div className="columns">
          <div className="column">
            <div className="is-flex">
              <Title title={title} /> <MarginLeft>{afterTitle}</MarginLeft>
            </div>
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

    const content: ReactNode[] = [];
    React.Children.forEach(children, child => {
      if (child) {
        if (!this.isPageAction(child)) {
          content.push(child);
        }
      }
    });
    return content;
  }
}

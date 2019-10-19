import * as React from 'react';
import classNames from 'classnames';
import styled from 'styled-components';
import Loading from './../Loading';
import ErrorNotification from './../ErrorNotification';
import Title from './Title';
import Subtitle from './Subtitle';
import PageActions from './PageActions';
import ErrorBoundary from '../ErrorBoundary';

type Props = {
  title?: string;
  subtitle?: string;
  loading?: boolean;
  error?: Error;
  showContentOnError?: boolean;
  children: React.Node;
};

const PageActionContainer = styled.div`
  justify-content: flex-end;
  align-items: center;

  // every child except first
  > * ~ * {
    margin-left: 1.25rem;
  }
`;

export default class Page extends React.Component<Props> {
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
      node.displayName === PageActions.displayName ||
      (node.type && node.type.displayName === PageActions.displayName)
    );
  }

  renderPageHeader() {
    const { error, title, subtitle, children } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && !error) {
        if (this.isPageAction(child)) {
          pageActions = (
            <PageActionContainer
              className={classNames(
                'column',
                'is-three-fifths',
                'is-mobile-action-spacing',
                'is-flex',
              )}
            >
              {child}
            </PageActionContainer>
          );
          pageActionsExists = true;
        }
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
        if (!this.isPageAction(child)) {
          content.push(child);
        }
      }
    });
    return content;
  }
}

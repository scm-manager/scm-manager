/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
  title?: ReactNode;
  // Use the documentTitle to set the document title (browser title) whenever you want to set one
  // and use something different than a string for the title property.
  documentTitle?: string;
  afterTitle?: ReactNode;
  subtitle?: ReactNode;
  loading?: boolean;
  error?: Error | null;
  showContentOnError?: boolean;
  children: ReactNode;
};

const PageActionContainer = styled.div`
  // every child except first
  > * ~ * {
    margin-left: 1.25rem;
  }
`;

export default class Page extends React.Component<Props> {
  componentDidUpdate() {
    const textualTitle = this.getTextualTitle();
    if (textualTitle && textualTitle !== document.title) {
      document.title = textualTitle;
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
    const { error, afterTitle, title, subtitle, children } = this.props;

    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, child => {
      if (child && !error) {
        if (this.isPageAction(child)) {
          pageActions = (
            <PageActionContainer
              className={classNames(
                "column",
                "is-three-quarters",
                "is-mobile-action-spacing",
                "is-flex-tablet",
                "is-justify-content-flex-end",
                "is-align-items-center"
              )}
            >
              {child}
            </PageActionContainer>
          );
          pageActionsExists = true;
        }
      }
    });
    const underline = pageActionsExists ? <hr className="header-with-actions" /> : null;

    if (title || subtitle) {
      return (
        <>
          <div className="columns">
            <div className="column">
              <div className="is-flex is-flex-wrap-wrap is-align-items-center">
                <Title className="mb-0 mr-2" title={this.getTextualTitle()}>
                  {this.getTitleComponent()}
                </Title>
                {afterTitle}
              </div>
              {subtitle ? <Subtitle>{subtitle}</Subtitle> : null}
            </div>
            {pageActions}
          </div>
          {underline}
        </>
      );
    }
    return null;
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

  getTextualTitle: () => string | undefined = () => {
    const { title, documentTitle } = this.props;
    if (documentTitle) {
      return documentTitle;
    } else if (typeof title === "string") {
      return title;
    } else {
      return undefined;
    }
  };

  getTitleComponent = () => {
    const { title } = this.props;
    if (title && typeof title !== "string") {
      return title;
    } else {
      return undefined;
    }
  };
}

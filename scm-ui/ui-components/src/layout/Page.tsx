/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import { devices } from "../devices";

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

const MaxTitleHeight = styled.div`
  // remove blank space in repo create form
  @media screen and (min-width: ${devices.tablet.width}px) {
    height: 2.25rem;
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
                "is-three-fifths",
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
              <MaxTitleHeight className="is-flex">
                <Title title={this.getTextualTitle()}>{this.getTitleComponent()}</Title>
                {afterTitle && <div className="ml-2">{afterTitle}</div>}
              </MaxTitleHeight>
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

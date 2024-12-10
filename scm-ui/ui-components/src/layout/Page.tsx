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

import React, { FC, ReactNode } from "react";
import classNames from "classnames";
import styled from "styled-components";
import Loading from "./../Loading";
import ErrorNotification from "./../ErrorNotification";
import ErrorBoundary from "../ErrorBoundary";
import Title from "./Title";
import Subtitle from "./Subtitle";
import PageActions from "./PageActions";

type Props = {
  title?: ReactNode;
  // [DEPRECATED] Use useDocumentTitle hook inside the component instead.
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

const Page: FC<Props> = ({ title, afterTitle, subtitle, loading, error, showContentOnError, children }) => {
  const isPageAction = (node: any) => {
    return (
      node.displayName === PageActions.displayName || (node.type && node.type.displayName === PageActions.displayName)
    );
  };

  const renderPageHeader = () => {
    let pageActions = null;
    let pageActionsExists = false;
    React.Children.forEach(children, (child) => {
      if (child && !error) {
        if (isPageAction(child)) {
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
                <Title className="mb-0 mr-2">{title}</Title>
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
  };

  const renderContent = () => {
    if (error && !showContentOnError) {
      return null;
    }
    if (loading) {
      return <Loading />;
    }

    const content: ReactNode[] = [];
    React.Children.forEach(children, (child) => {
      if (child) {
        if (!isPageAction(child)) {
          content.push(child);
        }
      }
    });
    return content;
  };

  return (
    <section className="section">
      <div className="container">
        {renderPageHeader()}
        <ErrorBoundary>
          <ErrorNotification error={error} />
          {renderContent()}
        </ErrorBoundary>
      </div>
    </section>
  );
};

export default Page;

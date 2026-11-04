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

import React, { FC, HTMLAttributes, ReactNode, Ref } from "react";
import classNames from "classnames";
import styled from "styled-components";
import { Loading, Subtitle, Title } from "../../../misc";
import { ErrorNotification } from "../../../notifications";

const ActionsColumn = styled.div`
  // every child except first
  > * ~ * {
    margin-left: 1.25rem;
  }
`;

type Props = Omit<HTMLAttributes<HTMLElement>, "title"> & {
  /**
   * Main heading of the page, rendered as `h1`.
   */
  title?: ReactNode;
  /**
   * Rendered as `h2` below the title.
   */
  subtitle?: ReactNode;
  /**
   * Rendered in line with the title, e.g. switches or flags.
   */
  afterTitle?: ReactNode;
  /**
   * Rendered in the right column of the header, e.g. filters and a create button.
   * The header is separated from the content by a line while actions are shown.
   * Actions are hidden while an `error` is set and need a `title` or `subtitle`.
   */
  actions?: ReactNode;
  /**
   * Shows a loading indicator instead of the content. The header stays visible.
   */
  loading?: boolean;
  /**
   * Shows an error notification below the header and hides the content and the actions.
   */
  error?: Error | null;
  /**
   * Keeps the content visible while an `error` is set.
   */
  showContentOnError?: boolean;
  ref?: Ref<HTMLElement>;
};

/**
 * Frame for a top-level page: a header with title, subtitle and actions, followed by the content.
 * It also handles the loading and the error state of the page.
 *
 * Set the document title with `useDocumentTitle` inside the page.
 *
 * @since 4.0.0
 */
const Page: FC<Props> = ({
  title,
  subtitle,
  afterTitle,
  actions,
  loading,
  error,
  showContentOnError,
  children,
  className,
  ref,
  ...props
}) => {
  const hasActions = !!actions && !error;

  let header = null;
  if (title || subtitle) {
    header = (
      <>
        <div className="columns">
          <div className="column">
            <div className="is-flex is-flex-wrap-wrap is-align-items-center">
              <Title className="mb-0 mr-2">{title}</Title>
              {afterTitle}
            </div>
            {subtitle ? <Subtitle>{subtitle}</Subtitle> : null}
          </div>
          {hasActions ? (
            <ActionsColumn
              className={classNames(
                "column",
                "is-two-thirds",
                "is-mobile-action-spacing",
                "is-flex-tablet",
                "is-justify-content-flex-end",
                "is-align-items-center",
              )}
            >
              {actions}
            </ActionsColumn>
          ) : null}
        </div>
        {hasActions ? <hr className="header-with-actions" /> : null}
      </>
    );
  }

  let content: ReactNode = children;
  if (error && !showContentOnError) {
    content = null;
  } else if (loading) {
    content = <Loading />;
  }

  return (
    <section className={classNames("section", className)} {...props} ref={ref}>
      <div className="container">
        {header}
        <ErrorNotification error={error} />
        {content}
      </div>
    </section>
  );
};

export default Page;

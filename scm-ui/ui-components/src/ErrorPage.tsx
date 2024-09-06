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

import React from "react";
import ErrorNotification from "./ErrorNotification";
import { BackendError, ForbiddenError } from "@scm-manager/ui-api";

type Props = {
  error: Error;
  title: string;
  subtitle: string;
};

class ErrorPage extends React.Component<Props> {
  render() {
    const { title, error } = this.props;

    return (
      <section className="section">
        <div className="box column is-4 is-offset-4 container">
          <h1 className="title">{title}</h1>
          {this.renderSubtitle()}
          <ErrorNotification error={error} />
        </div>
      </section>
    );
  }

  renderSubtitle = () => {
    const { error, subtitle } = this.props;
    if (error instanceof BackendError || error instanceof ForbiddenError) {
      return null;
    }
    return <p className="subtitle">{subtitle}</p>;
  };
}

export default ErrorPage;

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

import { Link } from "react-router-dom";
import React from "react";
import { Changeset, Repository } from "@scm-manager/ui-types";
import { createChangesetLink } from "./changesets";

type Props = {
  repository: Repository;
  changeset: Changeset;
  link: boolean;
};

export default class ChangesetId extends React.Component<Props> {
  static defaultProps = {
    link: true,
  };

  shortId = (changeset: Changeset) => {
    return changeset.id.substr(0, 7);
  };

  renderLink = () => {
    const { repository, changeset } = this.props;
    const link = createChangesetLink(repository, changeset);

    return <Link to={link}>{this.shortId(changeset)}</Link>;
  };

  renderText = () => {
    const { changeset } = this.props;
    return this.shortId(changeset);
  };

  render() {
    const { link } = this.props;
    if (link) {
      return this.renderLink();
    }
    return this.renderText();
  }
}

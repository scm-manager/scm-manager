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

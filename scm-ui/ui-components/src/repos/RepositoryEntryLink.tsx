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
import React from "react";
import { Link } from "react-router-dom";
import { Icon } from "@scm-manager/ui-components";
import Tooltip from "../Tooltip";

type Props = {
  to: string;
  icon: string;
  tooltip?: string;
};

class RepositoryEntryLink extends React.Component<Props> {
  render() {
    const { to, icon, tooltip } = this.props;

    let content = <Icon className="fa-lg" name={icon} color="inherit" alt={`${icon} icon`} />;
    if (tooltip) {
      content = (
        <Tooltip message={tooltip} location="top">
          {content}
        </Tooltip>
      );
    }

    return (
      <Link className="level-item is-clickable" to={to}>
        {content}
      </Link>
    );
  }
}

export default RepositoryEntryLink;

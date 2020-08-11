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
import * as React from "react";
import { Route, Link } from "react-router-dom";
import { createAttributesForTesting } from "../devBuild";

type Props = {
  to: string;
  label: string;
  match?: string;
  activeOnlyWhenExact?: boolean;
  testId?: string;
};

class PrimaryNavigationLink extends React.Component<Props> {
  renderLink = (route: any) => {
    const { to, label, testId } = this.props;
    return (
      <li className={route.match ? "is-active" : ""}>
        <Link to={to} {...createAttributesForTesting(testId)}>
          {label}
        </Link>
      </li>
    );
  };

  render() {
    const { to, match, activeOnlyWhenExact, testId } = this.props;
    const path = match ? match : to;
    return (
      <Route
        path={path}
        exact={activeOnlyWhenExact}
        children={this.renderLink}
        {...createAttributesForTesting(testId)}
      />
    );
  }
}

export default PrimaryNavigationLink;

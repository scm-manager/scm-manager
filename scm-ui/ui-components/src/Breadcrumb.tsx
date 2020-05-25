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
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Branch, Repository } from "@scm-manager/ui-types";
import Icon from "./Icon";

type Props = WithTranslation & {
  repository: Repository;
  branch: Branch;
  defaultBranch: Branch;
  revision: string;
  path: string;
  baseUrl: string;
  sources: File;
};

const FlexStartNav = styled.nav`
  flex: 1;
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const ActionWrapper = styled.div`
  align-self: center;
  padding-right: 1rem;
`;

class Breadcrumb extends React.Component<Props> {
  renderPath() {
    const { revision, path, baseUrl } = this.props;

    if (path) {
      const paths = path.split("/");
      return paths.map((pathFragment, index) => {
        const currPath = paths.slice(0, index + 1).join("/");
        if (paths.length - 1 === index) {
          return (
            <li className="is-active" key={index}>
              <Link to="#" aria-current="page">
                {pathFragment}
              </Link>
            </li>
          );
        }
        return (
          <li key={index}>
            <Link to={baseUrl + "/" + revision + "/" + currPath}>{pathFragment}</Link>
          </li>
        );
      });
    }
    return null;
  }

  render() {
    const { repository, baseUrl, branch, defaultBranch, sources, revision, path, t } = this.props;

    let homeUrl = baseUrl + "/";
    if (revision) {
      homeUrl += encodeURIComponent(revision) + "/";
    }

    return (
      <>
        <div className="is-flex">
          <FlexStartNav className={classNames("breadcrumb", "sources-breadcrumb")} aria-label="breadcrumbs">
            <ul>
              <li>
                <Link to={homeUrl}>
                  <HomeIcon title={t("breadcrumb.home")} name="home" color="inherit" />
                </Link>
              </li>
              {this.renderPath()}
            </ul>
          </FlexStartNav>
          {binder.hasExtension("repos.sources.actionbar") && (
            <ActionWrapper>
              <ExtensionPoint
                name="repos.sources.actionbar"
                props={{
                  baseUrl,
                  revision,
                  branch: branch ? branch : defaultBranch,
                  path,
                  sources,
                  repository
                }}
                renderAll={true}
              />
            </ActionWrapper>
          )}
        </div>
        <hr className="is-marginless" />
      </>
    );
  }
}

export default withTranslation("commons")(Breadcrumb);

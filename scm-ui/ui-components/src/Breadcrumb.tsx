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
import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation, Link } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Branch, Repository, File } from "@scm-manager/ui-types";
import Icon from "./Icon";
import Tooltip from "./Tooltip";
import copyToClipboard from "./CopyToClipboard";
import { urls } from "@scm-manager/ui-api";

type Props = {
  repository: Repository;
  branch?: Branch;
  defaultBranch?: Branch;
  revision: string;
  path: string;
  baseUrl: string;
  sources: File;
  preButtons?: React.ReactNode;
  permalink: string | null;
};

const PermaLinkWrapper = styled.span`
  width: 16px;
  height: 16px;
  font-size: 13px;

  i {
    color: #dbdbdb;
    opacity: 0.75;
  }

  &:hover i {
    color: #b5b5b5;
    opacity: 1;
  }
`;

const BreadcrumbNav = styled.nav`
  flex: 1;
  display: flex;
  align-items: center;
  margin: 1rem 1rem !important;
  
  width: 100%;

  /* move slash to end */
  li + li::before {
    content: none;
  }
  li:not(:last-child)::after {
    color: #b5b5b5; //$breadcrumb-item-separator-color
    content: "\\0002f";
  }
  li:first-child {
    margin-left: 0.75rem;
  }

  /* sizing of each item */
  li {
    max-width: 375px;
    a {
      display: initial;
    }
  }
`;

const HomeIcon = styled(Icon)`
  line-height: 1.5rem;
`;

const ActionBar = styled.div`
  align-self: center;

  /* order actionbar items horizontal */
  display: flex;
  justify-content: flex-start;

  /* ensure space between action bar items */

  & > * {
    /* 
     * We have to use important, because plugins could use field or control classes like the editor-plugin does.
     * Those classes overwrite the margin which is ok, if the plugin is the only one which is using the actionbar.
     * But it looks terrible if another plugin use the actionbar, which does not use field and control classes.
     */
    margin: 0 0.75rem 0 0 !important;
  }
`;

const PrefixButton = styled.div`
  border-right: 1px solid lightgray;
  margin-right: 0.5rem;
`;

const Breadcrumb: FC<Props> = ({
  repository,
  branch,
  defaultBranch,
  revision,
  path,
  baseUrl,
  sources,
  permalink,
  preButtons
}) => {
  const location = useLocation();
  const history = useHistory();
  const [copying, setCopying] = useState(false);
  const [t] = useTranslation("commons");

  const pathSection = () => {
    if (path) {
      const paths = path.split("/");
      return paths.map((pathFragment, index) => {
        const currPath = paths.slice(0, index + 1).join("/");
        if (paths.length - 1 === index) {
          return (
            <li className="is-active" key={index}>
              <Link className="is-ellipsis-overflow" to="#" aria-current="page">
                {pathFragment}
              </Link>
            </li>
          );
        }
        return (
          <li key={index}>
            <Link
              className="is-ellipsis-overflow"
              title={pathFragment}
              to={baseUrl + "/" + encodeURIComponent(revision) + "/" + currPath}
            >
              {pathFragment}
            </Link>
          </li>
        );
      });
    }
    return null;
  };

  const copySource = () => {
    history.push(location.pathname);
    setCopying(true);
    copyToClipboard(
      window.location.protocol + "//" + window.location.host + urls.withContextPath(permalink || location.pathname)
    ).finally(() => setCopying(false));
  };

  let homeUrl = baseUrl + "/";
  if (revision) {
    homeUrl += encodeURIComponent(revision) + "/";
  }

  let prefixButtons = null;
  if (preButtons) {
    prefixButtons = <PrefixButton>{preButtons}</PrefixButton>;
  }

  return (
    <>
      <div className="is-flex is-align-items-center">
        <BreadcrumbNav className={classNames("breadcrumb", "sources-breadcrumb", "ml-1", "mb-0")} aria-label="breadcrumbs">
          {prefixButtons}
          <ul>
            <li>
              <Link to={homeUrl}>
                <HomeIcon title={t("breadcrumb.home")} name="home" color="inherit" />
              </Link>
            </li>
            {pathSection()}
          </ul>
          <PermaLinkWrapper className="ml-1">
            {copying ? (
              <Icon name="spinner fa-spin" />
            ) : (
              <Tooltip message={t("breadcrumb.copyPermalink")}>
                <Icon name="link" color="inherit" onClick={() => copySource()} />
              </Tooltip>
            )}
          </PermaLinkWrapper>
        </BreadcrumbNav>
        {binder.hasExtension("repos.sources.actionbar") && (
          <ActionBar>
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
          </ActionBar>
        )}
      </div>
      <hr className="is-marginless" />
    </>
  );
};

export default Breadcrumb;

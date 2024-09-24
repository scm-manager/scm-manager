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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useHistory, useLocation } from "react-router-dom";
import classNames from "classnames";
import styled from "styled-components";
import { urls } from "@scm-manager/ui-api";
import { Branch, File, Repository } from "@scm-manager/ui-types";
import { binder, ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import Icon from "./Icon";
import Tooltip from "./Tooltip";
import copyToClipboard from "./CopyToClipboard";
import { devices } from "./devices";

type Props = {
  repository: Repository;
  branch?: Branch;
  defaultBranch?: Branch;
  clickable?: boolean;
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

  @media screen and (min-width: ${devices.tablet.width}px) {
    margin-left: 0.25rem;
  }
  @media screen and (max-width: ${devices.mobile.width}px) {
    margin-left: auto;
  }

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

  @media screen and (max-width: ${devices.mobile.width}px) {
    flex-wrap: wrap;
  }

  /* move slash to end */
  li + li::before {
    content: none;
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

const BreadcrumbNode: FC<{ clickable: boolean; text: string; url: string; current?: boolean }> = ({
  clickable,
  text,
  url,
  current
}) => {
  if (clickable) {
    return (
      <Link className="is-ellipsis-overflow" title={text} to={url} aria-current={current ? "page" : undefined}>
        {text}
      </Link>
    );
  } else {
    return (
      <span className="is-ellipsis-overflow has-text-inherit px-3" title={text}>
        {text}
      </span>
    );
  }
};

const Breadcrumb: FC<Props> = ({
  repository,
  branch,
  defaultBranch,
  revision,
  path,
  baseUrl,
  sources,
  permalink,
  preButtons,
  clickable = true
}) => {
  const location = useLocation();
  const history = useHistory();
  const [copying, setCopying] = useState(false);
  const [t] = useTranslation("commons");

  const pathSection = () => {
    if (path) {
      const paths = path.split("/");
      return paths.map((pathFragment, index) => {
        const decodedPathFragment = decodeURIComponent(pathFragment);
        let currPath = paths
          .slice(0, index + 1)
          .map(encodeURIComponent)
          .join("/");
        if (!currPath.endsWith("/")) {
          currPath = currPath + "/";
        }
        if (paths.length - 1 === index) {
          return (
            <li className="is-active" key={index}>
              <BreadcrumbNode clickable={clickable} text={decodedPathFragment} url="#" current={true} />
            </li>
          );
        }
        return (
          <li key={index}>
            <BreadcrumbNode
              clickable={clickable}
              text={decodedPathFragment}
              url={baseUrl + "/" + encodeURIComponent(revision) + "/" + currPath}
            />
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

  const renderBreadcrumbNav = () => {
    let prefixButtons = null;
    if (preButtons) {
      prefixButtons = <div className="mr-2 prefix-button">{preButtons}</div>;
    }

    let homeUrl = baseUrl + "/";
    if (revision) {
      homeUrl += encodeURIComponent(revision) + "/";
    } else {
      homeUrl = `/repo/${repository.namespace}/${repository.name}/code/sources/`;
    }

    return (
      <BreadcrumbNav
        className={classNames(
          "breadcrumb",
          "sources-breadcrumb",
          "mx-2",
          "my-4",
          "is-flex",
          "is-align-items-center",
          "is-full-width"
        )}
        aria-label="breadcrumbs"
      >
        {prefixButtons}
        <ul>
          <li>
            {clickable ? (
              <Link to={homeUrl} aria-label={t("breadcrumb.home")}>
                <HomeIcon title={t("breadcrumb.home")} name="home" color="inherit" />
              </Link>
            ) : (
              <Icon name="home" color="inherit" className="mr-3" />
            )}
          </li>
          {pathSection()}
        </ul>
        <PermaLinkWrapper tabIndex={0} onKeyDown={e => e.key === "Enter" && copySource()}>
          {copying ? (
            <Icon name="spinner fa-spin" alt={t("breadcrumb.loading")} />
          ) : (
            <Tooltip message={t("breadcrumb.copyPermalink")}>
              <Icon name="link" color="inherit" onClick={() => copySource()} alt={t("breadcrumb.copyPermalink")} />
            </Tooltip>
          )}
        </PermaLinkWrapper>
      </BreadcrumbNav>
    );
  };

  const extProps = {
    baseUrl,
    revision: revision ? encodeURIComponent(revision) : "",
    branch: branch ? branch : defaultBranch,
    path,
    sources,
    repository
  };

  const renderExtensionPoints = () => {
    if (
      binder.hasExtension<extensionPoints.ReposSourcesEmptyActionbar>("repos.sources.empty.actionbar", extProps) &&
      sources?._embedded?.children?.length === 0
    ) {
      return (
        <ExtensionPoint<extensionPoints.ReposSourcesEmptyActionbar>
          name="repos.sources.empty.actionbar"
          props={{ repository, sources }}
          renderAll={true}
        />
      );
    }
    if (binder.hasExtension<extensionPoints.ReposSourcesActionbar>("repos.sources.actionbar", extProps)) {
      return (
        <ExtensionPoint<extensionPoints.ReposSourcesActionbar>
          name="repos.sources.actionbar"
          props={extProps}
          renderAll={true}
        />
      );
    }
    return null;
  };

  return (
    <>
      <div
        className={classNames("is-flex", "is-justify-content-flex-end", "is-align-items-center", "is-flex-wrap-wrap")}
      >
        {renderBreadcrumbNav()}
        {
          <ActionBar
            className={classNames(
              "is-flex",
              "is-justify-content-flex-start",
              "is-align-self-center",
              "m-2",
              "is-flex-wrap-wrap"
            )}
          >
            {renderExtensionPoints()}
          </ActionBar>
        }
      </div>
      <hr className="m-0" />
    </>
  );
};

export default Breadcrumb;

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

import React, { FC, ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint, extensionPoints } from "@scm-manager/ui-extensions";
import { File, Link, Repository } from "@scm-manager/ui-types";
import { DateFromNow, ErrorNotification, FileSize, Icon, OpenInFullscreenButton } from "@scm-manager/ui-components";
import FileButtonAddons from "../components/content/FileButtonAddons";
import SourcesView from "./SourcesView";
import HistoryView from "./HistoryView";
import AnnotateView from "./AnnotateView";
import ContentActionMenu from "../components/content/overflowMenu/ContentActionMenu";
import { useContentType } from "@scm-manager/ui-api";
import { useAriaId } from "@scm-manager/ui-core";

type Props = {
  file: File;
  repository: Repository;
  revision: string;
  breadcrumb: React.ReactNode;
  error?: Error;
};

const HeaderWrapper = styled.div`
  border-bottom: 1px solid var(--scm-border-color);
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
`;

const BorderBottom = styled.div`
  border-bottom: 1px solid var(--scm-border-color);
`;

const FullWidthTitleHeader = styled.button`
  max-width: 100%;
  border: none;
  background-color: transparent;
`;

const BorderLessDiv = styled.div`
  margin: -1.25rem;
  border: none;
  box-shadow: none;
`;

export type SourceViewSelection = "source" | "annotations" | "history";

const Content: FC<Props> = ({ file, repository, revision, breadcrumb, error }) => {
  const [t] = useTranslation("repos");
  const [collapsed, setCollapsed] = useState(true);
  const [selected, setSelected] = useState<SourceViewSelection>("source");
  const [errorFromExtension, setErrorFromExtension] = useState<Error>();
  const { data: contentType } = useContentType((file._links.self as Link).href);
  const fileDetailsId = useAriaId();

  const wrapContent = (content: ReactNode) => {
    return (
      <>
        <div className="panel">
          {breadcrumb}
          {content}
        </div>
        <ErrorNotification error={errorFromExtension} />
      </>
    );
  };

  const toggleCollapse = () => {
    setCollapsed(!collapsed);
  };

  const showHeader = (content: ReactNode) => {
    let icon;
    if (collapsed) {
      icon = (
        <Icon
          className={classNames("is-inline", "mr-2")}
          name="angle-right fa-fw"
          color="inherit"
          alt={t("sources.content.showMore")}
        />
      );
    } else {
      icon = (
        <Icon
          className={classNames("is-inline", "mr-2")}
          name="angle-down fa-fw"
          color="inherit"
          alt={t("sources.content.hideMore")}
        />
      );
    }

    const selector = file._links.history ? (
      <FileButtonAddons
        file={file}
        className="mr-2"
        selected={selected}
        showSources={() => setSelected("source")}
        showAnnotations={() => setSelected("annotations")}
        showHistory={() => setSelected("history")}
      />
    ) : null;

    const extensionProps: extensionPoints.ContentActionExtensionProps = {
      repository,
      file,
      revision: revision ? encodeURIComponent(revision) : "",
      handleExtensionError: setErrorFromExtension,
      contentType,
    };

    return (
      <HeaderWrapper>
        <div className={classNames("level", "is-flex-wrap-wrap")}>
          <FullWidthTitleHeader
            className={classNames("is-word-break", "mr-2", "button", "p-0", "is-size-5", "has-text-weight-light")}
            onClick={toggleCollapse}
            aria-controls={fileDetailsId}
            aria-expanded={!collapsed}
          >
            {icon}
            {file.name}
          </FullWidthTitleHeader>
          <div className={classNames("level-right", "buttons", "ml-auto")}>
            {selector}
            <OpenInFullscreenButton
              modalTitle={file?.name}
              modalBody={<BorderLessDiv className="panel">{content}</BorderLessDiv>}
              tooltipStyle="htmlTitle"
            />
            <ExtensionPoint<extensionPoints.ReposSourcesContentActionBar>
              name="repos.sources.content.actionbar"
              props={extensionProps}
              renderAll={true}
            />
            <ContentActionMenu extensionProps={extensionProps} />
          </div>
        </div>
      </HeaderWrapper>
    );
  };

  const showMoreInformation = () => {
    const fileSize = file.directory ? null : <FileSize bytes={file?.length ? file.length : 0} />;
    const description = file.description ? (
      <p>
        {file.description.split("\n").map((item, key) => {
          return (
            <span key={key}>
              {item}
              <br />
            </span>
          );
        })}
      </p>
    ) : null;

    if (!collapsed) {
      return (
        <>
          <div className="panel-block has-background-secondary-less" id={fileDetailsId}>
            <table className="table has-background-secondary-less">
              <tbody>
                <tr>
                  <td>{t("sources.content.path")}</td>
                  <td className="is-word-break">{file.path}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.branch")}</td>
                  <td className="is-word-break">{revision}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.size")}</td>
                  <td>{fileSize}</td>
                </tr>
                <tr>
                  <td>{t("sources.content.commitDate")}</td>
                  <td>
                    <DateFromNow date={file.commitDate} />
                  </td>
                </tr>
                <tr>
                  <td>{t("sources.content.description")}</td>
                  <td className="is-word-break">{description}</td>
                </tr>
                <ExtensionPoint<extensionPoints.ReposContentMetaData>
                  name="repos.content.metadata"
                  renderAll={true}
                  props={{
                    file,
                    repository,
                    revision,
                  }}
                />
              </tbody>
            </table>
          </div>
          <BorderBottom />
        </>
      );
    }
    return null;
  };

  if (!file || error) {
    return wrapContent(<ErrorNotification error={error} />);
  }

  let body;
  switch (selected) {
    case "source":
      body = <SourcesView file={file} repository={repository} revision={revision} />;
      break;
    case "annotations":
      body = <AnnotateView file={file} repository={repository} revision={revision} />;
      break;
    case "history":
      body = <HistoryView file={file} repository={repository} revision={revision} />;
  }
  const header = showHeader(body);
  const moreInformation = showMoreInformation();

  return wrapContent(
    <>
      {header}
      {moreInformation}
      {body}
    </>
  );
};

export default Content;

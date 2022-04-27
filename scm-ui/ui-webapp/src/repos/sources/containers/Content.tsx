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
import ContentActionMenu from "./ContentActionMenu";
import { useContentType } from "@scm-manager/ui-api";

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

const FullWidthTitleHeader = styled.div`
  max-width: 100%;
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
      contentType
    };

    return (
      <HeaderWrapper>
        <div className={classNames("level", "is-flex-wrap-wrap")}>
          <FullWidthTitleHeader
            className={classNames("level-left", "is-flex", "is-clickable", "is-word-break", "mr-2")}
            onClick={toggleCollapse}
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
          <div className="panel-block has-background-secondary-less">
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
                    revision
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

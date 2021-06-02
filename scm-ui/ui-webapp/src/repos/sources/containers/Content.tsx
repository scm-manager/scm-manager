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
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import { DateFromNow, ErrorNotification, FileSize, Icon, OpenInFullscreenButton } from "@scm-manager/ui-components";
import FileButtonAddons from "../components/content/FileButtonAddons";
import SourcesView from "./SourcesView";
import HistoryView from "./HistoryView";
import AnnotateView from "./AnnotateView";

type Props = {
  file: File;
  repository: Repository;
  revision: string;
  path: string;
  breadcrumb: React.ReactNode;
  error?: Error;
};

const HeaderWrapper = styled.div`
  border-bottom: solid 1px #dbdbdb;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
`;

const LighterGreyBackgroundPanelBlock = styled.div`
  background-color: #fbfbfb;
`;

const LighterGreyBackgroundTable = styled.table`
  background-color: #fbfbfb;
`;

const BorderBottom = styled.div`
  border-bottom: solid 1px #dbdbdb;
`;

const FullWidthTitleHeader = styled.div`
  max-width: 100%;
`;

const AlignRight = styled.div`
  margin-left: auto;
`;

const BorderLessDiv = styled.div`
  margin: -1.25rem;
  border: none;
  box-shadow: none;
`;

export type SourceViewSelection = "source" | "annotations" | "history";

const Content: FC<Props> = ({ file, repository, revision, path, breadcrumb, error }) => {
  const [t] = useTranslation("repos");
  const [collapsed, setCollapsed] = useState(true);
  const [selected, setSelected] = useState<SourceViewSelection>("source");
  const [errorFromExtension, setErrorFromExtension] = useState<Error | null>(null);

  const wrapContent = (content: ReactNode) => {
    return (
      <>
        <div className="panel">
          {breadcrumb}
          {content}
        </div>
        {errorFromExtension && <ErrorNotification error={errorFromExtension} />}
      </>
    );
  };

  const toggleCollapse = () => {
    setCollapsed(!collapsed);
  };

  const handleExtensionError = (error: Error) => {
    setErrorFromExtension(error);
  };

  const showHeader = (content: ReactNode) => {
    const icon = collapsed ? "angle-right" : "angle-down";

    const selector = file._links.history ? (
      <FileButtonAddons
        className="mr-2"
        selected={selected}
        showSources={() => setSelected("source")}
        showAnnotations={() => setSelected("annotations")}
        showHistory={() => setSelected("history")}
      />
    ) : null;

    return (
      <HeaderWrapper>
        <div className={classNames("level", "is-flex-wrap-wrap")}>
          <FullWidthTitleHeader
            className={classNames("level-left", "is-flex", "has-cursor-pointer", "is-word-break", "mr-2")}
            onClick={toggleCollapse}
          >
            <Icon className={classNames("is-inline", "mr-2")} name={`${icon} fa-fw`} color="inherit" />
            {file.name}
          </FullWidthTitleHeader>
          <AlignRight className={classNames("level-right", "buttons")}>
            {selector}
            <OpenInFullscreenButton
              modalTitle={file?.name}
              modalBody={<BorderLessDiv className="panel">{content}</BorderLessDiv>}
              tooltipStyle="htmlTitle"
            />
            <ExtensionPoint
              name="repos.sources.content.actionbar"
              props={{
                repository,
                file,
                revision: revision ? encodeURIComponent(revision) : "",
                handleExtensionError: handleExtensionError,
              }}
              renderAll={true}
            />
          </AlignRight>
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
          <LighterGreyBackgroundPanelBlock className="panel-block">
            <LighterGreyBackgroundTable className="table">
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
                <ExtensionPoint
                  name="repos.content.metadata"
                  renderAll={true}
                  props={{
                    file,
                    repository,
                    revision,
                  }}
                />
              </tbody>
            </LighterGreyBackgroundTable>
          </LighterGreyBackgroundPanelBlock>
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
      body = <SourcesView revision={revision} file={file} repository={repository} path={path} />;
      break;
    case "annotations":
      body = <AnnotateView file={file} repository={repository} />;
      break;
    case "history":
      body = <HistoryView file={file} repository={repository} />;
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

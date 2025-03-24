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

import { FileTree } from "@scm-manager/ui-types";
import React, { FC } from "react";
import classNames from "classnames";
import { FileDiffContainer, FileDiffContent } from "./styledElements";
import { Icon } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

type Props = { tree: FileTree; currentFile: string; setCurrentFile: (path: string) => void };

const StyledIcon = styled(Icon)`
  min-width: 1.5rem;
`;
const StyledStatus = styled(StyledIcon)`
  margin-left: auto;
`;

const DiffFileTree: FC<Props> = ({ tree, currentFile, setCurrentFile }) => {
  return (
    <FileDiffContainer className={"mt-4 py-3 pr-2"}>
      <FileDiffContent>
        {Object.keys(tree.children).map((key) => (
          <TreeNode
            key={key}
            node={tree.children[key]}
            parentPath={""}
            currentFile={currentFile}
            setCurrentFile={setCurrentFile}
          />
        ))}
      </FileDiffContent>
    </FileDiffContainer>
  );
};

export default DiffFileTree;

type NodeProps = { node: FileTree; parentPath: string; currentFile: string; setCurrentFile: (path: string) => void };

const addPath = (parentPath: string, path: string) => {
  if ("" === parentPath) {
    return path;
  }
  return parentPath + "/" + path;
};

const TreeNode: FC<NodeProps> = ({ node, parentPath, currentFile, setCurrentFile }) => {
  const [t] = useTranslation("repos");

  return (
    <li>
      {Object.keys(node.children).length > 0 ? (
        <ul className={"py-1 pr-1 pl-3"}>
          <li className={"is-flex has-text-grey"}>
            <StyledIcon alt={t("diff.showContent")}>folder</StyledIcon>
            <div className={"ml-1"}>{node.nodeName}</div>
          </li>
          {Object.keys(node.children).map((key) => (
            <TreeNode
              key={key}
              node={node.children[key]}
              parentPath={addPath(parentPath, node.nodeName)}
              currentFile={currentFile}
              setCurrentFile={setCurrentFile}
            />
          ))}
        </ul>
      ) : (
        <TreeFile
          changeType={node.changeType.toLowerCase() as ChangeType}
          path={node.nodeName}
          parentPath={parentPath}
          currentFile={currentFile}
          setCurrentFile={setCurrentFile}
        />
      )}
    </li>
  );
};

type FileChangeTypeIconProps = {
  changeType: string;
};

const FileChangeTypeIcon: FC<FileChangeTypeIconProps> = ({ changeType }) => {
  const [t] = useTranslation("repos");
  if (changeType === "ADD") {
    return (
      <StyledStatus
        className="has-text-success"
        style={{ minWidth: "1rem", fontSize: "0.7rem", verticalAlign: "top" }}
        key={"add"}
        alt={t("diff.changes.add")}
      >
        plus
      </StyledStatus>
    );
  }
  if (changeType === "MODIFY") {
    return (
      <StyledStatus
        className="has-text-info"
        style={{ minWidth: "1.5rem", fontSize: "0.4rem", verticalAlign: "top" }}
        key={"modify"}
        alt={t("diff.changes.modify")}
      >
        circle
      </StyledStatus>
    );
  }
  if (changeType === "DELETE") {
    return (
      <StyledStatus
        className="has-text-danger"
        style={{ minWidth: "1.5rem", fontSize: "0.7rem", verticalAlign: "top" }}
        key={"delete"}
        alt={t("diff.changes.delete")}
      >
        minus
      </StyledStatus>
    );
  }
  if (changeType === "RENAME") {
    return (
      <StyledStatus
        className="has-text-info"
        style={{ minWidth: "1.5rem", fontSize: "0.4rem", verticalAlign: "top" }}
        key={"rename"}
        alt={t("diff.changes.rename")}
      >
        circle
      </StyledStatus>
    );
  }
  if (changeType === "COPY") {
    return (
      <StyledStatus
        className="has-text-info"
        style={{ minWidth: "1.5rem", fontSize: "0.7rem", verticalAlign: "top" }}
        key={"copy"}
        alt={t("diff.changes.copy")}
      >
        plus
      </StyledStatus>
    );
  }
  return null;
};

type ChangeType = "add" | "modify" | "delete" | "rename" | "copy";

const getColor = (changeType: ChangeType) => {
  switch (changeType) {
    case "add":
      return "success";
    case "modify":
    case "rename":
    case "copy":
      return "info";
    case "delete":
      return "danger";
  }
};
const getIcon = (changeType: ChangeType) => {
  switch (changeType) {
    case "add":
    case "copy":
      return "plus";
    case "modify":
    case "rename":
      return "circle";
    case "delete":
      return "minus";
  }
};

type FileProps = {
  changeType: string;
  path: string;
  parentPath: string;
  currentFile: string;
  setCurrentFile: (path: string) => void;
};

export const TreeFileContent = styled.div`
  cursor: pointer;
`;

const TreeFile: FC<FileProps> = ({ changeType, path, parentPath, currentFile, setCurrentFile }) => {
  const [t] = useTranslation("repos");
  const completePath = addPath(parentPath, path);

  const isCurrentFile = () => {
    return currentFile === completePath;
  };

  return (
    <TreeFileContent className={"is-flex py-1 pl-3"} onClick={() => setCurrentFile(completePath)}>
      {isCurrentFile() ? (
        <span className="fa-stack" style={{ minWidth: "1.5rem", maxWidth: "1.5rem" }}>
          <StyledIcon
            className="fa-stack-2x fa-xs"
            style={{ minWidth: "1.5rem", fontSize: "1.5rem" }}
            key={completePath + "file"}
            alt={t("diff.showContent")}
          >
            file
          </StyledIcon>
          <span className="fa-stack-1x fa-xs" style={{ minWidth: "1.5rem", maxWidth: "1.5rem" }}>
            <FileChangeTypeIcon changeType={changeType.toUpperCase()} />
          </span>
        </span>
      ) : (
        <span className="fa-stack" style={{width: "2.5em", height: "3em", fontSize: "0.5em"}}>
          <StyledIcon
            className={classNames("fa-stack-2x", `has-text-${getColor(changeType)}`)}
            key={completePath + "file"}
            type="far"
            alt={t("diff.showContent")}
          >
            file
          </StyledIcon>
          <StyledIcon
            className={classNames("fa-stack-1x", `has-text-${getColor(changeType)}`)}
            key={changeType}
            alt={t(`diff.changes.${changeType}`)}
          >
            {getIcon(changeType)}
          </StyledIcon>
        </span>
      )}
      <div className={"ml-1"}>{path}</div>
    </TreeFileContent>
  );
};

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

import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { Icon } from "@scm-manager/ui-core";
import { FileTree } from "@scm-manager/ui-types";
import { FileDiffContainer, FileDiffContent } from "./styledElements";

const StackedSpan = styled.span`
  width: 3em;
  height: 3em;
  font-size: 0.5em;
`;

const StyledIcon = styled(Icon)`
  min-width: 1.5rem;
`;

type Props = { tree: FileTree; currentFile: string; setCurrentFile: (path: string) => void };

const DiffFileTree: FC<Props> = ({ tree, currentFile, setCurrentFile }) => {
  return (
    <FileDiffContainer className="mt-4 py-3 pr-2">
      <FileDiffContent>
        {Object.keys(tree.children).map((key) => (
          <TreeNode
            key={key}
            node={tree.children[key]}
            parentPath=""
            currentFile={currentFile}
            setCurrentFile={setCurrentFile}
          />
        ))}
      </FileDiffContent>
    </FileDiffContainer>
  );
};

export default DiffFileTree;

type ChangeType = "add" | "modify" | "delete" | "rename" | "copy";

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
        <ul className="py-1 pr-1 pl-3">
          <li className="is-flex has-text-grey">
            <StyledIcon alt={t("diff.showContent")}>folder</StyledIcon>
            <div className="ml-1">{node.nodeName}</div>
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
  changeType: ChangeType;
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
    <TreeFileContent className="is-flex py-1 pl-3" onClick={() => setCurrentFile(completePath)}>
      {isCurrentFile() ? (
        <StackedSpan className="fa-stack">
          <StyledIcon
            className={classNames("fa-stack-2x", `has-text-${getColor(changeType)}`)}
            key={completePath + "file"}
            type="fas"
            alt={t("diff.showContent")}
          >
            file
          </StyledIcon>
          <StyledIcon
            className={classNames("fa-stack-1x", "has-text-secondary-least")}
            key={changeType}
            alt={t(`diff.changes.${changeType}`)}
          >
            {getIcon(changeType)}
          </StyledIcon>
        </StackedSpan>
      ) : (
        <StackedSpan className="fa-stack">
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
        </StackedSpan>
      )}
      <div className="ml-1">{path}</div>
    </TreeFileContent>
  );
};

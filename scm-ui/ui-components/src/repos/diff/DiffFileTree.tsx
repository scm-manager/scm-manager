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
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { FileChangeType, FileTree } from "@scm-manager/ui-types";
import { FileDiffContent, StackedSpan, StyledIcon } from "./styledElements";
import { FileTreeNodeWrapper } from "../DiffTypes";

type Props = {
  tree: FileTree;
  currentFile: string;
  setCurrentFile: (path: string) => void;
  gap?: number;
  FileTreeNodeWrapper?: FileTreeNodeWrapper;
};

const DiffFileTree: FC<Props> = ({ tree, currentFile, setCurrentFile, gap = 15, FileTreeNodeWrapper }) => {
  return (
    <FileDiffContent gap={gap}>
      {Object.keys(tree.children).map((key) => (
        <TreeNode
          key={key}
          node={tree.children[key]}
          parentPath=""
          currentFile={currentFile}
          setCurrentFile={setCurrentFile}
          FileTreeNodeWrapper={FileTreeNodeWrapper}
        />
      ))}
    </FileDiffContent>
  );
};

export default DiffFileTree;

type NodeProps = {
  node: FileTree;
  parentPath: string;
  currentFile: string;
  setCurrentFile: (path: string) => void;
  FileTreeNodeWrapper?: FileTreeNodeWrapper;
};

const addPath = (parentPath: string, path: string) => {
  if ("" === parentPath) {
    return path;
  }
  return parentPath + "/" + path;
};

const TreeNode: FC<NodeProps> = ({ node, parentPath, currentFile, setCurrentFile, FileTreeNodeWrapper }) => {
  const [t] = useTranslation("repos");

  FileTreeNodeWrapper = FileTreeNodeWrapper || (({ children }) => <>{children}</>);

  const label = <div className="ml-1">{node.nodeName}</div>;
  const icon = <StyledIcon alt={t("diff.showContent")}>folder</StyledIcon>;
  return (
    <li>
      {Object.keys(node.children).length > 0 ? (
        <ul className="py-1 pl-3">
          <li className="is-flex has-text-grey">
            <FileTreeNodeWrapper
              path={addPath(parentPath, node.nodeName)}
              isFile={false}
              isCurrentFile={false}
              name={node.nodeName}
              iconName={"folder"}
              iconColor={"grey"}
              originalIcon={icon}
              originalLabel={label}
            >
              {icon}
              {label}
            </FileTreeNodeWrapper>
          </li>
          {Object.keys(node.children).map((key) => (
            <TreeNode
              key={key}
              node={node.children[key]}
              parentPath={addPath(parentPath, node.nodeName)}
              currentFile={currentFile}
              setCurrentFile={setCurrentFile}
              FileTreeNodeWrapper={FileTreeNodeWrapper}
            />
          ))}
        </ul>
      ) : (
        <TreeFile
          changeType={node.changeType.toLowerCase() as FileChangeType}
          path={node.nodeName}
          parentPath={parentPath}
          currentFile={currentFile}
          setCurrentFile={setCurrentFile}
          FileTreeNodeWrapper={FileTreeNodeWrapper}
        />
      )}
    </li>
  );
};

const getColor = (changeType: FileChangeType) => {
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

const getIcon = (changeType: FileChangeType) => {
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
  changeType: FileChangeType;
  path: string;
  parentPath: string;
  currentFile: string;
  setCurrentFile: (path: string) => void;
  FileTreeNodeWrapper: FileTreeNodeWrapper;
};

const TreeFile: FC<FileProps> = ({
  changeType,
  path,
  parentPath,
  currentFile,
  setCurrentFile,
  FileTreeNodeWrapper,
}) => {
  const [t] = useTranslation("repos");
  const completePath = addPath(parentPath, path);

  const isCurrentFile = () => {
    return currentFile === completePath;
  };

  const iconName = getIcon(changeType);

  const icon = (
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
        className={classNames("fa-stack-1x", "is-relative", "has-text-secondary-least")}
        isSmaller={iconName === "circle"}
        key={changeType}
        alt={t(`diff.changes.${changeType}`)}
      >
        {iconName}
      </StyledIcon>
    </StackedSpan>
  );
  const label = <div className={classNames("ml-1", isCurrentFile() ? "has-text-weight-bold" : "")}>{path}</div>;

  return (
    <Link
      className="is-flex py-1 pl-3 has-cursor-pointer"
      onClick={() => setCurrentFile(completePath)}
      to={`#diff-${encodeURIComponent(completePath)}`}
    >
      <FileTreeNodeWrapper
        name={path}
        path={completePath}
        changeType={changeType}
        isFile={true}
        iconName={iconName}
        iconColor={getColor(changeType)}
        originalIcon={icon}
        originalLabel={label}
        isCurrentFile={isCurrentFile()}
      >
        {icon}
        {label}
      </FileTreeNodeWrapper>
    </Link>
  );
};

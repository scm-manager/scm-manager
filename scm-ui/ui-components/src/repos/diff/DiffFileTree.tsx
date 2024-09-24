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
import { FileDiffContainer, FileDiffContent, FileDiffContentTitle } from "./styledElements";
import { Icon } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

type Props = { tree: FileTree; currentFile: string; setCurrentFile: (path: string) => void };

const StyledIcon = styled(Icon)`
  min-width: 1.5rem;
`;

const DiffFileTree: FC<Props> = ({ tree, currentFile, setCurrentFile }) => {
  const [t] = useTranslation("repos");

  return (
    <FileDiffContainer className={"mt-4 py-3 pr-2"}>
      <FileDiffContentTitle className={"ml-4 pb-4 title is-6"}>{t("changesets.diffTreeTitle")}</FileDiffContentTitle>
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
          path={node.nodeName}
          parentPath={parentPath}
          currentFile={currentFile}
          setCurrentFile={setCurrentFile}
        />
      )}
    </li>
  );
};

type FileProps = { path: string; parentPath: string; currentFile: string; setCurrentFile: (path: string) => void };

export const TreeFileContent = styled.div`
  cursor: pointer;
`;

const TreeFile: FC<FileProps> = ({ path, parentPath, currentFile, setCurrentFile }) => {
  const [t] = useTranslation("repos");
  const completePath = addPath(parentPath, path);

  const isCurrentFile = () => {
    return currentFile === completePath;
  };

  return (
    <TreeFileContent className={"is-flex py-1 pl-3"} onClick={() => setCurrentFile(completePath)}>
      {isCurrentFile() ? (
        <StyledIcon style={{ minWidth: "1.5rem" }} key={completePath + "file"} alt={t("diff.showContent")}>
          file
        </StyledIcon>
      ) : (
        <StyledIcon style={{ minWidth: "1.5rem" }} key={completePath + "file"} type="far" alt={t("diff.showContent")}>
          file
        </StyledIcon>
      )}
      <div className={"ml-1"}>{path}</div>
    </TreeFileContent>
  );
};

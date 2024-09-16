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

export const TreeFileContent = styled.li`
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

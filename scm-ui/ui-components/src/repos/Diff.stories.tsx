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
import React, { ReactNode, useEffect, useState } from "react";
import { storiesOf } from "@storybook/react";
import Diff from "./Diff";
// @ts-ignore
import parser from "gitdiff-parser";
import simpleDiff from "../__resources__/Diff.simple";
import hunksDiff from "../__resources__/Diff.hunks";
import binaryDiff from "../__resources__/Diff.binary";
import { DiffEventContext, File } from "./DiffTypes";
import Toast from "../toast/Toast";
import { getPath } from "./diffs";
import DiffButton from "./DiffButton";
import styled from "styled-components";
import { MemoryRouter } from "react-router-dom";
import {one} from "../__resources__/changesets";

const diffFiles = parser.parse(simpleDiff);

const Container = styled.div`
  padding: 2rem 6rem;
`;

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

storiesOf("Diff", module)
  .addDecorator(RoutingDecorator)
  .addDecorator(storyFn => <Container>{storyFn()}</Container>)
  .add("Default", () => <Diff diff={diffFiles} changeset={one} />)
  .add("Side-By-Side", () => <Diff diff={diffFiles} sideBySide={true} changeset={one} />)
  .add("Collapsed", () => <Diff diff={diffFiles} defaultCollapse={true} changeset={one} />)
  .add("File Controls", () => (
    <Diff
      diff={diffFiles}
      changeset={one}
      fileControlFactory={() => (
        <DiffButton
          tooltip="A skull and crossbones or death's head is a symbol consisting of a human skull and two long bones crossed together under or behind the skull. The design originates in the Late Middle Ages as a symbol of death and especially as a memento mori on tombstones."
          icon="skull-crossbones"
          onClick={() => alert("Arrrgggghhhh ...")}
        />
      )}
    />
  ))
  .add("File Annotation", () => (
    <Diff
      diff={diffFiles}
      changeset={one}
      fileAnnotationFactory={file => [<p key={file.newPath}>Custom File annotation for {file.newPath}</p>]}
    />
  ))
  .add("Line Annotation", () => (
    <Diff
      diff={diffFiles}
      changeset={one}
      annotationFactory={ctx => {
        return {
          N2: <p key="N2">Line Annotation</p>
        };
      }}
    />
  ))
  .add("OnClick", () => {
    const OnClickDemo = () => {
      const [changeId, setChangeId] = useState();
      useEffect(() => {
        const interval = setInterval(() => setChangeId(undefined), 2000);
        return () => clearInterval(interval);
      });
      // @ts-ignore
      const onClick = (context: DiffEventContext) => setChangeId(context.changeId);
      return (
        <>
          {changeId && <Toast type="info" title={"Change " + changeId} />}
          <Diff diff={diffFiles} changeset={one} onClick={onClick} />
        </>
      );
    };
    return <OnClickDemo />;
  })
  .add("Hunks", () => {
    const hunkDiffFiles = parser.parse(hunksDiff);
    return <Diff diff={hunkDiffFiles} changeset={one} />;
  })
  .add("Binaries", () => {
    const binaryDiffFiles = parser.parse(binaryDiff);
    return <Diff diff={binaryDiffFiles} changeset={one} />;
  })
  .add("SyntaxHighlighting", () => {
    const filesWithLanguage = diffFiles.map((file: File) => {
      const ext = getPath(file).split(".")[1];
      if (ext === "tsx") {
        file.language = "typescript";
      } else {
        file.language = ext;
      }
      return file;
    });
    return <Diff diff={filesWithLanguage} changeset={one} />;
  })
  .add("CollapsingWithFunction", () => (
    <Diff diff={diffFiles} changeset={one} defaultCollapse={(oldPath, newPath) => oldPath.endsWith(".java")} />
  ))
  .add("Expandable", () => {
    const filesWithLanguage = diffFiles.map((file: File) => {
      file._links = { lines: { href: "http://example.com/" } };
      return file;
    });
    return <Diff diff={filesWithLanguage} changeset={one} />;
  })
  .add("WithLinkToFile", () => (
    <Diff
      diff={diffFiles}
      changeset={one}
      baseUrl="/repo/hitchhiker/heartOfGold/code/changeset"
    />
  ));

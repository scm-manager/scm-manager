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
import parser from "gitdiff-parser";
import simpleDiff from "../__resources__/Diff.simple";
import hunksDiff from "../__resources__/Diff.hunks";
import binaryDiff from "../__resources__/Diff.binary";
import markdownDiff from "../__resources__/Diff.markdown";
import { DiffEventContext, FileControlFactory } from "./DiffTypes";
import Toast from "../toast/Toast";
import { getPath } from "./diffs";
import DiffButton from "./DiffButton";
import styled from "styled-components";
import { MemoryRouter } from "react-router-dom";
import { two } from "../__resources__/changesets";
import { Changeset, FileDiff } from "@scm-manager/ui-types";
import JumpToFileButton from "./JumpToFileButton";
import Button from "../buttons/Button";
// @ts-ignore ignore unknown png
import hitchhikerImg from "../__resources__/hitchhiker.png";
// @ts-ignore ignore unknown jpg
import marvinImg from "../__resources__/marvin.jpg";

const diffFiles = parser.parse(simpleDiff);

const Container = styled.div`
  padding: 2rem 6rem;
`;

type ExternalDiffState = {
  [key: string]: boolean;
};

const RoutingDecorator = (story: () => ReactNode) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>;

const fileControlFactory: (changeset: Changeset) => FileControlFactory = (changeset) => (file) => {
  const baseUrl = "/repo/hitchhiker/heartOfGold/code/changeset";
  const sourceLink = {
    url: `${baseUrl}/${changeset.id}/${file.newPath}/`,
    label: "Jump to source",
  };
  const targetLink = changeset._embedded?.parents?.length === 1 && {
    url: `${baseUrl}/${changeset._embedded.parents[0].id}/${file.oldPath}`,
    label: "Jump to target",
  };

  const links = [];
  switch (file.type) {
    case "add":
      links.push(sourceLink);
      break;
    case "delete":
      if (targetLink) {
        links.push(targetLink);
      }
      break;
    default:
      if (targetLink) {
        links.push(sourceLink, targetLink);
      } else {
        links.push(sourceLink);
      }
  }

  return links.map(({ url, label }) => <JumpToFileButton tooltip={label} link={url} />);
};

storiesOf("Repositories/Diff", module)
  .addDecorator(RoutingDecorator)
  .addDecorator((storyFn) => <Container>{storyFn()}</Container>)
  .add("Default", () => <Diff diff={diffFiles} />)
  .add("Side-By-Side", () => <Diff diff={diffFiles} sideBySide={true} />)
  .add("Collapsed", () => <Diff diff={diffFiles} defaultCollapse={true} fileControlFactory={fileControlFactory(two)} />)
  .add("File Controls", () => (
    <Diff
      diff={diffFiles}
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
      fileAnnotationFactory={(file) => [<p key={file.newPath}>Custom File annotation for {file.newPath}</p>]}
    />
  ))
  .add("Line Annotation", () => (
    <Diff
      diff={diffFiles}
      annotationFactory={(ctx) => {
        return {
          N2: <p key="N2">Line Annotation</p>,
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
          <Diff diff={diffFiles} onClick={onClick} />
        </>
      );
    };
    return <OnClickDemo />;
  })
  .add("Hunks", () => {
    const hunkDiffFiles = parser.parse(hunksDiff);
    return <Diff diff={hunkDiffFiles} />;
  })
  .add("Hunk gutter hover icon", () => {
    const hunkDiffFiles = parser.parse(hunksDiff);
    return <Diff diff={hunkDiffFiles} hunkGutterHoverIcon="\f075" />;
  })
  .add("Highlight line on hover", () => {
    const hunkDiffFiles = parser.parse(hunksDiff);
    return <Diff diff={hunkDiffFiles} highlightLineOnHover />;
  })
  .add("Binaries", () => {
    const binaryDiffFiles = parser.parse(binaryDiff);
    return <Diff diff={binaryDiffFiles} />;
  })
  .add("Images", () => {
    const binaryDiffFiles: FileDiff[] = [
      {
        type: "add",
        newPath: "test.png",
        oldPath: "/dev/null",
        isBinary: true,
        newEndingNewLine: false,
        oldEndingNewLine: false,
        _links: {
          newFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
        },
      },
      {
        type: "delete",
        newPath: "/dev/null",
        oldPath: "test.png",
        isBinary: true,
        newEndingNewLine: false,
        oldEndingNewLine: false,
        _links: {
          oldFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
        },
      },
      {
        type: "modify",
        newPath: "test.png",
        oldPath: "test.png",
        isBinary: true,
        newEndingNewLine: false,
        oldEndingNewLine: false,
        _links: {
          oldFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
          newFile: {
            href: `${window.location.protocol}//${window.location.host}/${marvinImg}`,
          },
        },
      },
      {
        type: "rename",
        newPath: "test.png",
        oldPath: "newFileName.png",
        isBinary: true,
        newEndingNewLine: false,
        oldEndingNewLine: false,
        _links: {
          oldFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
          newFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
        },
      },
      {
        type: "copy",
        newPath: "test.png",
        oldPath: "newFileName.png",
        isBinary: true,
        newEndingNewLine: false,
        oldEndingNewLine: false,
        _links: {
          oldFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
          newFile: {
            href: `${window.location.protocol}//${window.location.host}/${hitchhikerImg}`,
          },
        },
      },
    ];
    return <Diff diff={binaryDiffFiles} />;
  })
  .add("SyntaxHighlighting", () => {
    const filesWithLanguage = diffFiles.map((file: FileDiff) => {
      const ext = getPath(file).split(".")[1];
      if (ext === "tsx") {
        file.language = "typescript";
      } else {
        file.language = ext;
      }
      return file;
    });
    return <Diff diff={filesWithLanguage} />;
  })
  .add("SyntaxHighlighting (Markdown)", () => {
    // @ts-ignore
    return <Diff diff={markdownDiff.files} />;
  })
  .add("CollapsingWithFunction", () => (
    <Diff diff={diffFiles} defaultCollapse={(oldPath, newPath) => oldPath.endsWith(".java")} />
  ))
  .add("Expandable", () => {
    const filesWithLanguage = diffFiles.map((file: FileDiff) => {
      file._links = { lines: { href: "http://example.com/" } };
      return file;
    });
    return <Diff diff={filesWithLanguage} />;
  })
  .add("WithLinkToFile", () => <Diff diff={diffFiles} />)
  .add("Changing Content", () => {
    const ChangingContentDiff = () => {
      const [markdown, setMarkdown] = useState(false);
      return (
        <div>
          <Button className="mb-5" action={() => setMarkdown((m) => !m)}>
            Change content
          </Button>
          {/* @ts-ignore */}
          <Diff diff={markdown ? markdownDiff.files : diffFiles} />
        </div>
      );
    };

    return <ChangingContentDiff />;
  })
  .add("External state management", () => {
    const [externalState, setExternalState] = useState<ExternalDiffState>({});

    const isCollapsed = (file: FileDiff) => {
      return externalState[file.newPath] || false;
    };

    const onCollapseStateChange = (file: FileDiff) => {
      setExternalState((current) => ({ ...current, [file.newPath]: !current[file.newPath] }));
    };

    return <Diff diff={diffFiles} isCollapsed={isCollapsed} onCollapseStateChange={onCollapseStateChange} />;
  });

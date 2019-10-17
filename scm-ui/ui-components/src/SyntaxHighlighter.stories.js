// @flow
import React, { useEffect, useState } from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import SyntaxHighlighter from "./SyntaxHighlighter";
import Loading from "./Loading";

const Spacing = styled.div`
  padding: 1em;
`;

type Props = {
  url: string,
  language: string
};

const LoadingSyntaxHighlighter = ({ url, language }: Props) => {
  const [content, setContent] = useState(undefined);
  useEffect(() => {
    fetch(url)
      .then(response => response.text())
      .then(setContent);
  });
  if (content) {
    return <SyntaxHighlighter language={language} value={content} />;
  } else {
    return <Loading />;
  }
};

storiesOf("SyntaxHighlighter", module)
  .add("Java", () => (
    <Spacing>
      <LoadingSyntaxHighlighter language="java" url="/HttpServer.java" />
    </Spacing>
  ))
  .add("Go", () => (
    <Spacing>
      <LoadingSyntaxHighlighter language="go" url="/HttpServer.go" />
    </Spacing>
  ))
  .add("Javascript", () => (
    <Spacing>
      <LoadingSyntaxHighlighter language="javascript" url="/HttpServer.js" />
    </Spacing>
  ))
  .add("Python", () => (
    <Spacing>
      <LoadingSyntaxHighlighter language="python" url="/HttpServer.py" />
    </Spacing>
  ));

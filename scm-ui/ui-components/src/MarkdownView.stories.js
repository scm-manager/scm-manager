// @flow
import React, {useState, useEffect} from "react";
import { storiesOf } from "@storybook/react";
import MarkdownView from "./MarkdownView";
import Loading from "./Loading";
import styled from "styled-components";
import {MemoryRouter} from "react-router-dom";

type Props = {
  url: string
};

const DataFetchingMarkdownView = ({url}: Props) => {
  const [content, setContent] = useState("")
  useEffect(() => {
    fetch(url)
      .then(response => response.text())
      .then(setContent);
  });

  if (content) {
    return <MarkdownView content={content} skipHtml={false} />;
  } else {
    return <Loading />;
  }
};

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("MarkdownView", module)
  .addDecorator(story => (
    <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>
  ))
  .add("Default", () => (
  <Spacing>
    <DataFetchingMarkdownView url="/test-page.md" />
  </Spacing>
));

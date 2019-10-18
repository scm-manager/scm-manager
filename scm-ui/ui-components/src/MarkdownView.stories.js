// @flow
import React from "react";
import {storiesOf} from "@storybook/react";
import MarkdownView from "./MarkdownView";
import styled from "styled-components";
import {MemoryRouter} from "react-router-dom";

import TestPage from "./__resources__/test-page.md";

const Spacing = styled.div`
  padding: 2em;
`;

storiesOf("MarkdownView", module)
  .addDecorator(story => (
    <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>
  ))
  .add("Default", () => (
  <Spacing>
    <MarkdownView content={TestPage} skipHtml={false} />
  </Spacing>
));

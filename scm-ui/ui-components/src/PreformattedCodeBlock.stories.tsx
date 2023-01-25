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
import * as React from "react";
import { storiesOf } from "@storybook/react";
import styled from "styled-components";
import PreformattedCodeBlock from "./PreformattedCodeBlock";
import { SubSubtitle } from "./layout";

const Wrapper = styled.div``;

const longContent =
  "#!/bin/bash\n" +
  "\n" +
  "### For this hook to work you need the SCM CLI client (https://scm-manager.org/cli/)\n" +
  "### installed and connected to your SCM Server.\n" +
  "\n" +
  "BRANCH_NAME=$(git symbolic-ref --short HEAD)\n" +
  "COMMIT_MSG_FILE=`cat $1`\n" +
  "\n" +
  'scm repo commit-message-check aaa/ultimate-repo $BRANCH_NAME "$COMMIT_MSG_FILE"';

storiesOf("PreformattedCodeBlock", module)
  .addDecorator((storyFn) => <Wrapper className="m-6">{storyFn()}</Wrapper>)
  .add("Default", () => <PreformattedCodeBlock>git checkout main</PreformattedCodeBlock>)
  .add("With scrollbar", () => (
    <PreformattedCodeBlock>
      git remote add origin
      https://scm-manager-instance4.example.org:8081/scm/repo/my-new-namespace/LoremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnaaliquyameratseddiamvoluptuaAtveroeosetaccusametjustoduodoloresetearebumStetclitakasdgubergrennoseatakimatasanctusestLoremipsumdolorsitametLoremipsumdolorsitametconsetetursadipscingelitrseddiamnonumyeirmodtemporinviduntutlaboreetdoloremagnaaliquyameratseddiamvoluptuaAtveroeosetaccusametjustoduodoloresetearebumStetclitakasdgubergrennoseatakimatasanctusestLoremipsumdolorsitamet
    </PreformattedCodeBlock>
  ))
  .add("Long content", () => <PreformattedCodeBlock>{longContent}</PreformattedCodeBlock>)
  .add("Combination", () => (
    <div className="content">
      <SubSubtitle>Clone the Repository</SubSubtitle>
      <PreformattedCodeBlock>git clone https://fancy-scm.url/scm/repo/test/scmm\n cd scmm</PreformattedCodeBlock>
      <SubSubtitle>Git hook for commit message validation</SubSubtitle>
      <PreformattedCodeBlock>{longContent}</PreformattedCodeBlock>
      <SubSubtitle>Get Remote Changes</SubSubtitle>
      <PreformattedCodeBlock>git fetch</PreformattedCodeBlock>
      <SubSubtitle>Switch Branch</SubSubtitle>
      <PreformattedCodeBlock>git checkout feature/something</PreformattedCodeBlock>
    </div>
  ));

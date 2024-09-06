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

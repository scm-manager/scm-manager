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
import React from "react";
import { storiesOf } from "@storybook/react";
import SplitAndReplace from "./SplitAndReplace";
import { Icon } from "@scm-manager/ui-components";
import styled from "styled-components";

const Wrapper = styled.div`
  margin: 2rem;
`;

storiesOf("SplitAndReplace", module).add("Simple replacement", () => {
  const replacements = [
    {
      textToReplace: "'",
      replacement: <Icon name={"quote-left"} />,
      replaceAll: true
    },
    {
      textToReplace: "`",
      replacement: <Icon name={"quote-right"} />,
      replaceAll: true
    }
  ];
  return (
    <>
      <Wrapper>
        <SplitAndReplace text={"'So this is it,` said Arthur, 'We are going to die.`"} replacements={replacements} />
      </Wrapper>
      <Wrapper>
        <SplitAndReplace text={"'Yes,` said Ford, 'except... no! Wait a minute!`"} replacements={replacements} />
      </Wrapper>
    </>
  );
});

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
import { ComponentMeta, ComponentStory } from "@storybook/react";
import SplitAndReplace from "./SplitAndReplace";

export default {
  title: "SplitAndReplace",
  component: SplitAndReplace,
} as ComponentMeta<typeof SplitAndReplace>;

const textOne = "'So this is it,` said Arthur, 'We are going to die.`";
const textTwo = "'Yes,` said Ford, 'except... no! Wait a minute!`";

export const ReplaceOnce: ComponentStory<typeof SplitAndReplace> = () => {
  const replacements = [
    {
      textToReplace: "'",
      replacement: <span>!</span>,
      replaceAll: false,
    },
  ];
  return (
    <div>
      <h3>Replace ' with ! once</h3>
      <label>Original text:</label>
      <p>{textOne}</p>
      <label>Text with replacements:</label>
      <p>
        <SplitAndReplace text={textOne} replacements={replacements} />
      </p>
    </div>
  );
};

export const ReplaceAll: ComponentStory<typeof SplitAndReplace> = () => {
  const replacements = [
    {
      textToReplace: "'",
      replacement: <span>!</span>,
      replaceAll: true,
    },
    {
      textToReplace: "`",
      replacement: <span>?</span>,
      replaceAll: true,
    },
  ];
  return (
    <div>
      <h3>Replace all ` with ? and ' with !</h3>
      <label>Original text:</label>
      <p>{textTwo}</p>

      <label> Text with replacements: </label>
      <p>
        <SplitAndReplace text={textTwo} replacements={replacements} />
      </p>
    </div>
  );
};

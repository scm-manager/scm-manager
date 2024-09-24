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

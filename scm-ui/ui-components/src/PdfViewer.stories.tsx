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
import PdfViewer from "./PdfViewer";
// @ts-ignore no need to declare module for a single import
import pdf from "./__resources__/doc.pdf";
import { storiesOf } from "@storybook/react";

storiesOf("PdfViewer", module)
  .add("Simple", () => <PdfViewer src={pdf} />)
  .add("Error", () => <PdfViewer src="/does/not/exists" />)
  .add("Error with download URL", () => <PdfViewer src="/does/not/exists" download={pdf} />);

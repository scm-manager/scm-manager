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

// WebWorker which creates tokens for syntax highlighting
// @ts-ignore typescript does not know the css module syntax
import theme from "./syntax-highlighting.module.css";
import type { LoadThemeRequest } from "./types";

const worker = new Worker(
  // @ts-ignore typscript does not know the import.meta object
  new URL("./worker/SyntaxHighlighter.worker.ts", import.meta.url),
  {
    name: "SyntaxHighlighter",
    type: "module",
  }
);

worker.postMessage({ type: "theme", payload: theme } as LoadThemeRequest);

const useSyntaxHighlightingWorker = (): Worker => worker;
export default useSyntaxHighlightingWorker;

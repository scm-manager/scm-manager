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
import {
  createRemark2RehypeCodeRendererAdapter,
  createRemark2RehypeHeadingRendererAdapterFactory,
  createRemark2RehypeLinkRendererAdapter,
} from "./remarkToRehypeRendererAdapters";

export type CreateComponentListOptions = {
  permalink?: string;
};

export const createComponentList = (remarkRendererList: any, { permalink }: CreateComponentListOptions) => {
  const components: any = {};

  if (remarkRendererList.code) {
    components.pre = createRemark2RehypeCodeRendererAdapter(remarkRendererList.code);
  }

  if (remarkRendererList.link) {
    components.a = createRemark2RehypeLinkRendererAdapter(remarkRendererList.link);
  }

  if (remarkRendererList.heading) {
    const createHeadingRendererAdapter = createRemark2RehypeHeadingRendererAdapterFactory(
      remarkRendererList.heading,
      permalink
    );
    components.h1 = createHeadingRendererAdapter(1);
    components.h2 = createHeadingRendererAdapter(2);
    components.h3 = createHeadingRendererAdapter(3);
    components.h4 = createHeadingRendererAdapter(4);
    components.h5 = createHeadingRendererAdapter(5);
    components.h6 = createHeadingRendererAdapter(6);
  }

  if (remarkRendererList.break) {
    components.br = remarkRendererList.break;
  }

  if (remarkRendererList.delete) {
    components.del = remarkRendererList.delete;
  }

  if (remarkRendererList.emphasis) {
    components.em = remarkRendererList.emphasis;
  }

  if (remarkRendererList.blockquote) {
    components.blockquote = remarkRendererList.blockquote;
  }

  if (remarkRendererList.image) {
    components.img = remarkRendererList.image;
  }

  if (remarkRendererList.list) {
    components.ol = remarkRendererList.list;
    components.ul = remarkRendererList.list;
  }

  if (remarkRendererList.listItem) {
    components.li = remarkRendererList.listItem;
  }

  if (remarkRendererList.paragraph) {
    components.p = remarkRendererList.paragraph;
  }

  if (remarkRendererList.strong) {
    components.strong = remarkRendererList.strong;
  }

  if (remarkRendererList.table) {
    components.table = remarkRendererList.table;
  }

  if (remarkRendererList.tableHead) {
    components.thead = remarkRendererList.tableHead;
  }

  if (remarkRendererList.tableBody) {
    components.tbody = remarkRendererList.tableBody;
  }

  if (remarkRendererList.tableRow) {
    components.tr = remarkRendererList.tableRow;
  }

  if (remarkRendererList.tableCell) {
    components.td = remarkRendererList.tableCell;
    components.th = remarkRendererList.tableCell;
  }

  if (remarkRendererList.thematicBreak) {
    components.hr = remarkRendererList.thematicBreak;
  }

  return components;
};

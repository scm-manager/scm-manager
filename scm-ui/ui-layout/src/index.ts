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

import CardListComponent, { CardListBox as CardListBoxComponent, CardListCard } from "./card-list/CardList";
import CardTitle from "./card/CardTitle";
import CardRow from "./card/CardRow";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "./card/CardDetail";
import CardComponent from "./card/Card";

export { default as Collapsible } from "./collapsible/Collapsible";

const CardExport = {
  Title: CardTitle,
  Row: CardRow,
  Details: Object.assign(CardDetails, {
    Detail: Object.assign(CardDetail, {
      Label: CardDetailLabel,
      Tag: CardDetailTag,
    }),
  }),
};

export const Card = Object.assign(CardComponent, CardExport);

const CardListExport = {
  Card: Object.assign(CardListCard, CardExport),
};

export const CardList = Object.assign(CardListComponent, CardListExport);
export const CardListBox = Object.assign(CardListBoxComponent, CardListExport);

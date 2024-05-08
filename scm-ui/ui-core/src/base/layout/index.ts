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
import CardRow, { SecondaryRow, TertiaryRow } from "./card/CardRow";
import {
  CardButtonDetail,
  CardDetail,
  CardDetailLabel,
  CardDetails,
  CardDetailTag,
  CardLinkDetail,
  CardVariants,
  CardVariant
} from "./card/CardDetail";
import CardComponent from "./card/Card";
import {
  DataPageHeader as DataPageHeaderComponent,
  DataPageHeaderCreateButton,
  DataPageHeaderSetting,
  DataPageHeaderSettingField,
  DataPageHeaderSettingLabel,
  DataPageHeaderSettings
} from "./templates/data-page/DataPageHeader";
import TabsComponent from "./tabs/Tabs";
import TabsContent from "./tabs/TabsContent";
import TabsList from "./tabs/TabsList";
import TabTrigger from "./tabs/TabTrigger";

export { default as Collapsible } from "./collapsible/Collapsible";

const CardDetailExports = {
  Label: CardDetailLabel,
  Tag: CardDetailTag,
};

const CardExport = {
  Title: CardTitle,
  Row: CardRow,
  SecondaryRow: SecondaryRow,
  TertiaryRow: TertiaryRow,
  Details: Object.assign(CardDetails, {
    Detail: Object.assign(CardDetail, CardDetailExports),
    ButtonDetail: Object.assign(CardButtonDetail, CardDetailExports),
    LinkDetail: Object.assign(CardLinkDetail, CardDetailExports),
  }),
};

export const Card = Object.assign(CardComponent, CardExport);

const CardListExport = {
  Card: Object.assign(CardListCard, CardExport),
};

export const CardList = Object.assign(CardListComponent, CardListExport);
export const CardListBox = Object.assign(CardListBoxComponent, CardListExport);

export const DataPageHeader = Object.assign(DataPageHeaderComponent, {
  Settings: Object.assign(DataPageHeaderSettings, {
    Setting: Object.assign(DataPageHeaderSetting, {
      Label: DataPageHeaderSettingLabel,
      Field: DataPageHeaderSettingField,
    }),
  }),
  CreateButton: DataPageHeaderCreateButton,
});

export const Tabs = Object.assign(TabsComponent, {
  List: Object.assign(TabsList, {
    Trigger: TabTrigger,
  }),
  Content: TabsContent,
});

export { CardVariants } from "./card/CardDetail";
export { CardVariant } from "./card/CardDetail";


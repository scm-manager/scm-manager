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


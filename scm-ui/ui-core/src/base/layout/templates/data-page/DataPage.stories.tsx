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

// import React from "react";
// import {
//   DataPageHeader,
//   DataPageHeaderCreateButton,
//   DataPageHeaderSetting,
//   DataPageHeaderSettingField,
//   DataPageHeaderSettingLabel,
//   DataPageHeaderSettings,
// } from "./DataPageHeader";
// import { Select } from "../../../forms";
// import { ComponentMeta, ComponentStory } from "@storybook/react";
// import { Loading, Subtitle, Title } from "../../../misc";
// import { ErrorNotification, Notification } from "../../../notifications";
// import { Button, Icon } from "../../../buttons";
// import { CardListBox, CardListCard } from "../../card-list/CardList";
// import CardRow, { SecondaryRow, TertiaryRow } from "../../card/CardRow";
// import CardTitle from "../../card/CardTitle";
// import { Link } from "react-router-dom";
// import StoryRouter from "storybook-react-router";
// import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "../../card/CardDetail";
//
// export default {
//   title: "Data Page Template",
//   component: DataPageHeader,
//   decorators: [StoryRouter()],
// } as ComponentMeta<typeof DataPageHeader>;
//
// // @ts-ignore Storybook is not cooperating
// export const Example: ComponentStory<{ error: Error; isLoading: boolean; isEmpty: boolean }> = ({
//   error,
//   isLoading,
//   isEmpty,
// }: any) => {
//   let content;
//   if (error) {
//     content = <ErrorNotification error={error} />;
//   } else if (isLoading) {
//     content = <Loading />;
//   } else if (isEmpty) {
//     content = <Notification type="info">There is no data, consider adjusting the filters</Notification>;
//   } else {
//     content = (
//       <CardListBox>
//         <CardListCard avatar={<Icon>trash</Icon>} action={<Icon>ellipsis-v</Icon>}>
//           <CardRow>
//             <CardTitle>
//               <Link to="/item">
//                 The title may contain a link but most importantly does not contain any information except the "display
//                 name" of the entity. It is also text-only
//               </Link>
//             </CardTitle>
//           </CardRow>
//           <SecondaryRow>
//             This contains more important details about the card, but not quite as important as the title.
//           </SecondaryRow>
//           <TertiaryRow>This contains less important information about the card</TertiaryRow>
//           <CardRow>
//             <CardDetails>
//               <CardDetail>
//                 <CardDetailLabel>Tags are great for numbers.</CardDetailLabel>
//                 <CardDetailTag>7/3</CardDetailTag>
//               </CardDetail>
//               <CardDetail>
//                 {({ labelId }) => (
//                   <>
//                     <CardDetailLabel id={labelId}>
//                       Interactive details need 'is-relative' and 'aria-labelledby'
//                     </CardDetailLabel>
//                     <Button aria-labelledby={labelId} className="is-relative has-background-transparent is-borderless">
//                       <Icon>edit</Icon>
//                     </Button>
//                   </>
//                 )}
//               </CardDetail>
//             </CardDetails>
//           </CardRow>
//         </CardListCard>
//         <CardListCard avatar={<Icon>users</Icon>} action={<Icon>ellipsis-v</Icon>}>
//           <CardRow>
//             <CardTitle>
//               <Link to="/item">
//                 We can also enter insane text without whitespace
//                 ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss
//               </Link>
//             </CardTitle>
//           </CardRow>
//           <SecondaryRow>
//             SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM
//           </SecondaryRow>
//           <TertiaryRow>
//             SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM
//           </TertiaryRow>
//           <CardRow>
//             <CardDetails>
//               <CardDetail>
//                 <CardDetailLabel>SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM</CardDetailLabel>
//                 <CardDetailTag>7/3</CardDetailTag>
//               </CardDetail>
//               <CardDetail>
//                 {({ labelId }) => (
//                   <>
//                     <CardDetailLabel id={labelId}>SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM</CardDetailLabel>
//                     <Button aria-labelledby={labelId} className="is-relative has-background-transparent is-borderless">
//                       <Icon>trash</Icon>
//                     </Button>
//                   </>
//                 )}
//               </CardDetail>
//             </CardDetails>
//           </CardRow>
//         </CardListCard>
//       </CardListBox>
//     );
//   }
//
//   return (
//     <>
//       <Title>My Page</Title>
//       <Subtitle subtitle="All the data" />
//       <DataPageHeader>
//         <DataPageHeaderSettings>
//           <DataPageHeaderSetting>
//             {({ formFieldId }) => (
//               <>
//                 <DataPageHeaderSettingLabel htmlFor={formFieldId}>Filter by</DataPageHeaderSettingLabel>
//                 <DataPageHeaderSettingField>
//                   <Select
//                     id={formFieldId}
//                     options={[
//                       {
//                         label: "Yes",
//                         value: 1,
//                       },
//                       {
//                         label: "No",
//                         value: 0,
//                       },
//                     ]}
//                   />
//                 </DataPageHeaderSettingField>
//               </>
//             )}
//           </DataPageHeaderSetting>
//           <DataPageHeaderSetting>
//             {({ formFieldId }) => (
//               <>
//                 <DataPageHeaderSettingLabel htmlFor={formFieldId}>Sort by</DataPageHeaderSettingLabel>
//                 <DataPageHeaderSettingField>
//                   <Select
//                     id={formFieldId}
//                     options={[
//                       {
//                         label: "Blue",
//                         value: 1,
//                       },
//                       {
//                         label: "Red",
//                         value: 0,
//                       },
//                     ]}
//                   />
//                 </DataPageHeaderSettingField>
//               </>
//             )}
//           </DataPageHeaderSetting>
//         </DataPageHeaderSettings>
//         <DataPageHeaderCreateButton to="/mydata/create">Create New Data</DataPageHeaderCreateButton>
//       </DataPageHeader>
//       {content}
//     </>
//   );
// };
// Example.args = {
//   error: undefined,
//   isLoading: false,
//   isEmpty: false,
// };

import React, { FC, ReactNode } from "react";
import type { Meta, StoryObj } from "@storybook/react";

import {
  DataPageHeader,
  DataPageHeaderSetting,
  DataPageHeaderSettingField,
  DataPageHeaderSettingLabel,
  DataPageHeaderSettings,
} from "./DataPageHeader";
import { Select } from "../../../forms";
import { Loading, Subtitle, Title } from "../../../misc";
import { ErrorNotification, Notification } from "../../../notifications";
import { Button, Icon } from "../../../buttons";
import { CardListBox, CardListCard } from "../../card-list/CardList";
import CardRow, { SecondaryRow, TertiaryRow } from "../../card/CardRow";
import CardTitle from "../../card/CardTitle";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "../../card/CardDetail";

const PageTemplate: FC<{ children: ReactNode }> = ({ children }) => (
  <>
    <Title>My Page</Title>
    <Subtitle subtitle="All the data" />
    <DataPageHeader>
      <DataPageHeaderSettings>
        <DataPageHeaderSetting>
          {({ formFieldId }) => (
            <>
              <DataPageHeaderSettingLabel htmlFor={formFieldId}>Filter by</DataPageHeaderSettingLabel>
              <DataPageHeaderSettingField>
                <Select
                  id={formFieldId}
                  options={[
                    { label: "Yes", value: 1 },
                    { label: "No", value: 0 },
                  ]}
                />
              </DataPageHeaderSettingField>
            </>
          )}
        </DataPageHeaderSetting>
        <DataPageHeaderSetting>
          {({ formFieldId }) => (
            <>
              <DataPageHeaderSettingLabel htmlFor={formFieldId}>Sort by</DataPageHeaderSettingLabel>
              <DataPageHeaderSettingField>
                <Select
                  id={formFieldId}
                  options={[
                    { label: "Blue", value: 1 },
                    { label: "Red", value: 0 },
                  ]}
                />
              </DataPageHeaderSettingField>
            </>
          )}
        </DataPageHeaderSetting>
      </DataPageHeaderSettings>
      <Button variant="primary">Create New Data</Button>
    </DataPageHeader>
    <div style={{ marginTop: "1rem" }}>{children}</div>
  </>
);

const dataContent = (
  <CardListBox>
    <CardListCard avatar={<Icon>trash</Icon>} action={<Icon>ellipsis-v</Icon>}>
      <CardRow>
        <CardTitle>
          <a href="/item" onClick={(e) => e.preventDefault()}>
            The title may contain a link but most importantly does not contain any information except the "display name"
            of the entity.
          </a>
        </CardTitle>
      </CardRow>
      <SecondaryRow>This contains more important details about the card.</SecondaryRow>
      <TertiaryRow>This contains less important information about the card</TertiaryRow>
      <CardRow>
        <CardDetails>
          <CardDetail>
            <CardDetailLabel>Tags are great for numbers.</CardDetailLabel>
            <CardDetailTag>7/3</CardDetailTag>
          </CardDetail>
        </CardDetails>
      </CardRow>
    </CardListCard>
  </CardListBox>
);

const meta: Meta<typeof DataPageHeader> = {
  title: "Templates/Data Page",
  component: DataPageHeader,
  decorators: [(Story) => <div style={{ margin: "2rem" }}>{Story()}</div>],
  tags: ["autodocs"],
};

export default meta;

type Story = StoryObj<typeof DataPageHeader>;

export const Default: Story = {
  name: "With Data",
  render: () => <PageTemplate>{dataContent}</PageTemplate>,
};

export const IsLoading: Story = {
  name: "Is Loading",
  render: () => (
    <PageTemplate>
      <Loading />
    </PageTemplate>
  ),
};

export const IsEmpty: Story = {
  name: "Is Empty",
  render: () => (
    <PageTemplate>
      <Notification type="info">There is no data, consider adjusting the filters</Notification>
    </PageTemplate>
  ),
};

export const WithError: Story = {
  name: "With Error",
  render: () => (
    <PageTemplate>
      <ErrorNotification error={new Error("Failed to fetch data from the server.")} />
    </PageTemplate>
  ),
};

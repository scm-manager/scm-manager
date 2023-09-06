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
import {
  DataPageHeader,
  DataPageHeaderCreateButton,
  DataPageHeaderSetting,
  DataPageHeaderSettingField,
  DataPageHeaderSettingLabel,
  DataPageHeaderSettings,
} from "./DataPageHeader";
import { Select } from "@scm-manager/ui-forms";
import { ComponentMeta, ComponentStory } from "@storybook/react";
import { ErrorNotification, Loading, Subtitle, Title, Notification } from "@scm-manager/ui-components";
import { Button, Icon } from "@scm-manager/ui-buttons";
import { CardListBox, CardListCard } from "../../card-list/CardList";
import CardRow, { SecondaryRow, TertiaryRow } from "../../card/CardRow";
import CardTitle from "../../card/CardTitle";
import { Link } from "react-router-dom";
import StoryRouter from "storybook-react-router";
import { CardDetail, CardDetailLabel, CardDetails, CardDetailTag } from "../../card/CardDetail";

export default {
  title: "Data Page Template",
  component: DataPageHeader,
  decorators: [StoryRouter()],
} as ComponentMeta<typeof DataPageHeader>;

// @ts-ignore Storybook is not cooperating
export const Example: ComponentStory<{ error: Error; isLoading: boolean; isEmpty: boolean }> = ({
  error,
  isLoading,
  isEmpty,
}: any) => {
  let content;
  if (error) {
    content = <ErrorNotification error={error} />;
  } else if (isLoading) {
    content = <Loading />;
  } else if (isEmpty) {
    content = <Notification type="info">There is no data, consider adjusting the filters</Notification>;
  } else {
    content = (
      <CardListBox>
        <CardListCard avatar={<Icon>trash</Icon>} action={<Icon>ellipsis-v</Icon>}>
          <CardRow>
            <CardTitle>
              <Link to="/item">
                The title may contain a link but most importantly does not contain any information except the "display
                name" of the entity. It is also text-only
              </Link>
            </CardTitle>
          </CardRow>
          <SecondaryRow>
            This contains more important details about the card, but not quite as important as the title.
          </SecondaryRow>
          <TertiaryRow>This contains less important information about the card</TertiaryRow>
          <CardRow>
            <CardDetails>
              <CardDetail>
                <CardDetailLabel>Tags are great for numbers.</CardDetailLabel>
                <CardDetailTag>7/3</CardDetailTag>
              </CardDetail>
              <CardDetail>
                {({ labelId }) => (
                  <>
                    <CardDetailLabel id={labelId}>
                      Interactive details need 'is-relative' and 'aria-labelledby'
                    </CardDetailLabel>
                    <Button aria-labelledby={labelId} className="is-relative has-background-transparent is-borderless">
                      <Icon>edit</Icon>
                    </Button>
                  </>
                )}
              </CardDetail>
            </CardDetails>
          </CardRow>
        </CardListCard>
        <CardListCard avatar={<Icon>users</Icon>} action={<Icon>ellipsis-v</Icon>}>
          <CardRow>
            <CardTitle>
              <Link to="/item">
                We can also enter insane text without whitespace
                ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss
              </Link>
            </CardTitle>
          </CardRow>
          <SecondaryRow>
            SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM
          </SecondaryRow>
          <TertiaryRow>
            SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM
          </TertiaryRow>
          <CardRow>
            <CardDetails>
              <CardDetail>
                <CardDetailLabel>SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM</CardDetailLabel>
                <CardDetailTag>7/3</CardDetailTag>
              </CardDetail>
              <CardDetail>
                {({ labelId }) => (
                  <>
                    <CardDetailLabel id={labelId}>SCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCMSCM</CardDetailLabel>
                    <Button aria-labelledby={labelId} className="is-relative has-background-transparent is-borderless">
                      <Icon>trash</Icon>
                    </Button>
                  </>
                )}
              </CardDetail>
            </CardDetails>
          </CardRow>
        </CardListCard>
      </CardListBox>
    );
  }

  return (
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
                      {
                        label: "Yes",
                        value: 1,
                      },
                      {
                        label: "No",
                        value: 0,
                      },
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
                      {
                        label: "Blue",
                        value: 1,
                      },
                      {
                        label: "Red",
                        value: 0,
                      },
                    ]}
                  />
                </DataPageHeaderSettingField>
              </>
            )}
          </DataPageHeaderSetting>
        </DataPageHeaderSettings>
        <DataPageHeaderCreateButton to="/mydata/create">Create New Data</DataPageHeaderCreateButton>
      </DataPageHeader>
      {content}
    </>
  );
};
Example.args = {
  error: undefined,
  isLoading: false,
  isEmpty: false,
};

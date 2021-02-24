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
import React, { FC } from "react";
import GroupForm from "../components/GroupForm";
import { DisplayedUser, Group, Link } from "@scm-manager/ui-types";
import { apiClient, ErrorNotification } from "@scm-manager/ui-components";
import DeleteGroup from "./DeleteGroup";
import { useIndexLinks, useUpdateGroup } from "@scm-manager/ui-api";
import { Redirect } from "react-router-dom";

type Props = {
  group: Group;
};

const EditGroup: FC<Props> = ({ group }) => {
  const indexLinks = useIndexLinks();
  const { error, isLoading, update, isUpdated } = useUpdateGroup();
  const autocompleteLink = (indexLinks.autocomplete as Link[]).find(i => i.name === "users");

  if (isUpdated) {
    return <Redirect to={`/group/${group.name}`} />;
  }

  // TODO: Replace with react-query hook
  const loadUserAutocompletion = (inputValue: string) => {
    if (!autocompleteLink) {
      return [];
    }
    const url = autocompleteLink.href + "?q=";
    return apiClient
      .get(url + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map((element: DisplayedUser) => {
          return {
            value: element,
            label: `${element.displayName} (${element.id})`
          };
        });
      });
  };

  return (
    <div>
      <ErrorNotification error={error || undefined} />
      <GroupForm group={group} submitForm={update} loading={isLoading} loadUserSuggestions={loadUserAutocompletion} />
      <DeleteGroup group={group} />
    </div>
  );
};

export default EditGroup;

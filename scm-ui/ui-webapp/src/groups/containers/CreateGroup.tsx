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
import { Redirect } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useCreateGroup, useUserSuggestions } from "@scm-manager/ui-api";
import { Page } from "@scm-manager/ui-components";
import GroupForm from "../components/GroupForm";

const CreateGroup: FC = () => {
  const [t] = useTranslation("groups");
  const { isLoading, create, error, group } = useCreateGroup();
  const userSuggestions = useUserSuggestions();

  if (group) {
    return <Redirect to={`/group/${group.name}`} />;
  }

  return (
    <Page title={t("addGroup.title")} subtitle={t("addGroup.subtitle")} error={error || undefined}>
      <div>
        <GroupForm submitForm={create} loading={isLoading} loadUserSuggestions={userSuggestions} />
      </div>
    </Page>
  );
};

export default CreateGroup;

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
import { useTranslation } from "react-i18next";
import { ErrorNotification, Loading, Subtitle, Title } from "@scm-manager/ui-components";
import RepositoryRoleForm from "./RepositoryRoleForm";
import { useCreateRepositoryRole } from "@scm-manager/ui-api";
import { Redirect } from "react-router-dom";

const CreateRepositoryRole: FC = () => {
  const [t] = useTranslation("admin");
  const { error, isLoading: loading, create, repositoryRole: created } = useCreateRepositoryRole();

  if (created) {
    return <Redirect to={`/admin/role/${created.name}/info`} />;
  }

  if (loading) {
    return <Loading />;
  } else if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <>
      <Title title={t("repositoryRole.title")} />
      <Subtitle subtitle={t("repositoryRole.createSubtitle")} />
      <RepositoryRoleForm submitForm={create} />
    </>
  );
};

export default CreateRepositoryRole;

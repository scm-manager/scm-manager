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

import { Link, Me, User } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import ApiKeyTable from "./ApiKeyTable";
import AddApiKey from "./AddApiKey";
import { useTranslation } from "react-i18next";
import { useApiKeys, useDeleteApiKey } from "@scm-manager/ui-api";
import { Link as RouterLink } from "react-router-dom";

type Props = {
  user: User | Me;
};

const SetApiKeys: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const { isLoading, data: apiKeys, error: fetchError } = useApiKeys(user);
  const { error: deletionError, isLoading: isDeleting, remove } = useDeleteApiKey(user);
  const error = deletionError || fetchError;

  const createLink = (apiKeys?._links?.create as Link)?.href;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!apiKeys || isLoading || isDeleting) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("apiKey.subtitle")} />
      <p>
        {t("apiKey.text1")} <RouterLink to={"/admin/roles/"}>{t("apiKey.manageRoles")}</RouterLink>
      </p>
      <p>{t("apiKey.text2")}</p>
      <br />
      <ApiKeyTable apiKeys={apiKeys} onDelete={remove} />
      {createLink && <AddApiKey user={user} apiKeys={apiKeys} />}
    </>
  );
};

export default SetApiKeys;

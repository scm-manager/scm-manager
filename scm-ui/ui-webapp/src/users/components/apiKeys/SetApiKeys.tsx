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

import { Collection, Links, User, Me } from "@scm-manager/ui-types";
import React, { FC, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient, ErrorNotification, Loading } from "@scm-manager/ui-components";
import ApiKeyTable from "./ApiKeyTable";
import AddApiKey from "./AddApiKey";
import { useTranslation } from "react-i18next";
import styled from "styled-components";

export type ApiKeysCollection = Collection & {
  _embedded: {
    keys: ApiKey[];
  };
};

export type ApiKey = {
  id: string;
  displayName: string;
  permissionRole: string;
  created: string;
  _links: Links;
};

export const CONTENT_TYPE_API_KEY = "application/vnd.scmm-apiKey+json;v=2";

type Props = {
  user: User | Me;
};

const Subtitle = styled.div`
  margin-bottom: 1rem;
`;

const SetApiKeys: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const [error, setError] = useState<undefined | Error>();
  const [loading, setLoading] = useState(false);
  const [apiKeys, setApiKeys] = useState<ApiKeysCollection | undefined>(undefined);

  useEffect(() => {
    fetchApiKeys();
  }, [user]);

  const fetchApiKeys = () => {
    setLoading(true);
    apiClient
      .get((user._links.apiKeys as Link).href)
      .then(r => r.json())
      .then(setApiKeys)
      .then(() => setLoading(false))
      .catch(setError);
  };

  const onDelete = (link: string) => {
    apiClient
      .delete(link)
      .then(fetchApiKeys)
      .catch(setError);
  };

  const createLink = (apiKeys?._links?.create as Link)?.href;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <div className={"media-content"}>
        <p>{t("apiKey.text1")} <Link to={"/admin/roles/"}>{t("apiKey.manageRoles")}</Link></p>
        <p>{t("apiKey.text2")}</p>
      </div>
      <hr />
      <ApiKeyTable apiKeys={apiKeys} onDelete={onDelete} />
      <hr />
      <Subtitle className={"media-content"}><h2 className={"title is-4"}>Create new key</h2></Subtitle>
      {createLink && <AddApiKey createLink={createLink} refresh={fetchApiKeys} />}
    </>
  );
};

export default SetApiKeys;

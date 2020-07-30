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

import { Collection, Link, Links, User, Me } from "@scm-manager/ui-types";
import React, { FC, useEffect, useState } from "react";
import AddPublicKey from "./AddPublicKey";
import PublicKeyTable from "./PublicKeyTable";
import { apiClient, ErrorNotification, Loading } from "@scm-manager/ui-components";

export type PublicKeysCollection = Collection & {
  _embedded: {
    keys: PublicKey[];
  };
};

export type PublicKey = {
  id: string;
  displayName: string;
  raw: string;
  created?: string;
  _links: Links;
};

export const CONTENT_TYPE_PUBLIC_KEY = "application/vnd.scmm-publicKey+json;v=2";

type Props = {
  user: User | Me;
};

const SetPublicKeys: FC<Props> = ({ user }) => {
  const [error, setError] = useState<undefined | Error>();
  const [loading, setLoading] = useState(false);
  const [publicKeys, setPublicKeys] = useState<PublicKeysCollection | undefined>(undefined);

  useEffect(() => {
    fetchPublicKeys();
  }, [user]);

  const fetchPublicKeys = () => {
    setLoading(true);
    apiClient
      .get((user._links.publicKeys as Link).href)
      .then(r => r.json())
      .then(setPublicKeys)
      .then(() => setLoading(false))
      .catch(setError);
  };

  const onDelete = (link: string) => {
    apiClient
      .delete(link)
      .then(fetchPublicKeys)
      .catch(setError);
  };

  const createLink = (publicKeys?._links?.create as Link)?.href;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  return (
    <>
      <PublicKeyTable publicKeys={publicKeys} onDelete={onDelete} />
      {createLink && <AddPublicKey createLink={createLink} refresh={fetchPublicKeys} />}
    </>
  );
};

export default SetPublicKeys;

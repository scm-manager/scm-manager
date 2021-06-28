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
import { useTranslation } from "react-i18next";
import AddPublicKey from "./AddPublicKey";
import PublicKeyTable from "./PublicKeyTable";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import { useDeletePublicKey, usePublicKeys } from "@scm-manager/ui-api";

type Props = {
  user: User | Me;
};

const SetPublicKeys: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  const { error: fetchingError, isLoading, data: publicKeys } = usePublicKeys(user);
  const { error: deletionError, remove } = useDeletePublicKey(user);
  const error = fetchingError || deletionError;

  const createLink = (publicKeys?._links?.create as Link)?.href;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (!publicKeys || isLoading) {
    return <Loading />;
  }

  return (
    <>
      <Subtitle subtitle={t("publicKey.subtitle")} />
      <p>{t("publicKey.description")}</p>
      <br />
      <PublicKeyTable publicKeys={publicKeys} onDelete={remove} />
      {createLink && <AddPublicKey publicKeys={publicKeys} user={user} />}
    </>
  );
};

export default SetPublicKeys;

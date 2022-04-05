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
import PublicKeyEntry from "./PublicKeyEntry";
import { Notification } from "@scm-manager/ui-components";
import { DeleteFunction } from "@scm-manager/ui-api";
import { PublicKey, PublicKeysCollection } from "@scm-manager/ui-types";

type Props = {
  publicKeys?: PublicKeysCollection;
  onDelete: DeleteFunction<PublicKey>;
};

const PublicKeyTable: FC<Props> = ({ publicKeys, onDelete }) => {
  const [t] = useTranslation("users");

  if (publicKeys?._embedded?.keys?.length === 0) {
    return <Notification type="info">{t("publicKey.noStoredKeys")}</Notification>;
  }

  return (
    <table className="card-table table is-hoverable is-fullwidth">
      <thead>
        <tr>
          <th>{t("publicKey.displayName")}</th>
          <th className="is-hidden-mobile">{t("publicKey.created")}</th>
          <th className="is-hidden-mobile">{t("publicKey.raw")}</th>
          <th />
        </tr>
      </thead>
      <tbody>
        {publicKeys?._embedded?.keys?.map((publicKey: PublicKey, index: number) => (
          <PublicKeyEntry key={index} onDelete={onDelete} publicKey={publicKey} />
        ))}
      </tbody>
    </table>
  );
};

export default PublicKeyTable;

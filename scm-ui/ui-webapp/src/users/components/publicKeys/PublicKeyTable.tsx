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

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

import { Button, ConfirmAlert, DateFromNow } from "@scm-manager/ui-components";
import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, PublicKey } from "@scm-manager/ui-types";
import { DeleteFunction } from "@scm-manager/ui-api";

type Props = {
  publicKey: PublicKey;
  onDelete: DeleteFunction<PublicKey>;
};

export const PublicKeyEntry: FC<Props> = ({ publicKey, onDelete }) => {
  const [t] = useTranslation("users");
  const [showModal, setShowModal] = useState(false);

  let deleteButton;
  if (publicKey?._links?.delete) {
    deleteButton = (
      <Button
        color="text"
        icon="trash"
        action={() => setShowModal(true)}
        title={t("publicKey.delete")}
        className="px-2"
      />
    );
  }

  return (
    <>
      <tr>
        <td className="is-vertical-align-middle">{publicKey.displayName}</td>
        <td className="is-vertical-align-middle is-hidden-mobile">
          <DateFromNow date={publicKey.created} />
        </td>
        <td className="is-vertical-align-middle is-hidden-mobile">
          {publicKey._links?.raw ? (
            <a title={t("publicKey.download")} href={(publicKey._links.raw as Link).href}>
              {publicKey.id}
            </a>
          ) : (
            publicKey.id
          )}
        </td>
        <td className="is-vertical-align-middle has-text-centered">{deleteButton}</td>
      </tr>
      {showModal ? (
        <ConfirmAlert
          title={t("publicKey.deleteConfirmAlert.title")}
          message={t("publicKey.deleteConfirmAlert.message")}
          buttons={[
            {
              label: t("publicKey.deleteConfirmAlert.submit"),
              onClick: () => {
                onDelete(publicKey);
                setShowModal(false);
              },
            },
            {
              className: "is-info",
              label: t("publicKey.deleteConfirmAlert.cancel"),
              onClick: () => setShowModal(false),
              autofocus: true,
            },
          ]}
          close={() => setShowModal(false)}
        />
      ) : null}
    </>
  );
};

export default PublicKeyEntry;

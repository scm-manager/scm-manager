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

import React, { FC, useState } from "react";
import { ConfirmAlert, Button, DateFromNow } from "@scm-manager/ui-components";
import { ApiKey } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";
import { DeleteFunction } from "@scm-manager/ui-api";

type Props = {
  apiKey: ApiKey;
  onDelete: DeleteFunction<ApiKey>;
};

export const ApiKeyEntry: FC<Props> = ({ apiKey, onDelete }) => {
  const [t] = useTranslation("users");
  const [showModal, setShowModal] = useState(false);
  let deleteButton;
  if (apiKey?._links?.delete) {
    deleteButton = (
      <Button color="text" icon="trash" action={() => setShowModal(true)} title={t("apiKey.delete")} className="px-2" />
    );
  }

  return (
    <>
      <tr>
        <td className="is-vertical-align-middle">{apiKey.displayName}</td>
        <td className="is-vertical-align-middle">{apiKey.permissionRole}</td>
        <td className="is-vertical-align-middle is-hidden-mobile">
          <DateFromNow date={apiKey.created} />
        </td>
        <td className="is-vertical-align-middle has-text-centered">{deleteButton}</td>
      </tr>
      {showModal ? (
        <ConfirmAlert
          title={t("apiKey.deleteConfirmAlert.title")}
          message={t("apiKey.deleteConfirmAlert.message")}
          buttons={[
            {
              label: t("apiKey.deleteConfirmAlert.submit"),
              onClick: () => {
                onDelete(apiKey);
                setShowModal(false);
              },
            },
            {
              className: "is-info",
              label: t("apiKey.deleteConfirmAlert.cancel"),
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

export default ApiKeyEntry;

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

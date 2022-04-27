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
import { DateFromNow, Icon } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { Link, PublicKey } from "@scm-manager/ui-types";
import { DeleteFunction } from "@scm-manager/ui-api";

type Props = {
  publicKey: PublicKey;
  onDelete: DeleteFunction<PublicKey>;
};

export const PublicKeyEntry: FC<Props> = ({ publicKey, onDelete }) => {
  const [t] = useTranslation("users");

  let deleteButton;
  if (publicKey?._links?.delete) {
    deleteButton = (
      <Icon name="trash" className="has-hover-secondary-invert p-1" title={t("publicKey.delete")} onClick={() => onDelete(publicKey)} />
    );
  }

  return (
    <>
      <tr>
        <td>{publicKey.displayName}</td>
        <td className="is-hidden-mobile">
          <DateFromNow date={publicKey.created} />
        </td>
        <td className="is-hidden-mobile">
          {publicKey._links?.raw ? (
            <a title={t("publicKey.download")} href={(publicKey._links.raw as Link).href}>
              {publicKey.id}
            </a>
          ) : (
            publicKey.id
          )}
        </td>
        <td className="has-text-centered">{deleteButton}</td>
      </tr>
    </>
  );
};

export default PublicKeyEntry;

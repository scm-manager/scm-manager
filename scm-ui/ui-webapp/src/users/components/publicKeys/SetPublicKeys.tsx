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

import { Link, Me, User } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import AddPublicKey from "./AddPublicKey";
import PublicKeyTable from "./PublicKeyTable";
import { ErrorNotification, Loading, Subtitle } from "@scm-manager/ui-components";
import { useDeletePublicKey, usePublicKeys } from "@scm-manager/ui-api";
import { useDocumentTitle } from "@scm-manager/ui-core";

type Props = {
  user: User | Me;
};

const SetPublicKeys: FC<Props> = ({ user }) => {
  const [t] = useTranslation("users");
  useDocumentTitle(t("singleUser.menu.setPublicKeyNavLink"), user.displayName);
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

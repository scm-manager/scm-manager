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
import { ErrorNotification, InputField, Loading, Subtitle } from "@scm-manager/ui-components";
import { Button, Level } from "@scm-manager/ui-core";
import { useTranslation } from "react-i18next";
import RoleSelector from "../../../repos/permissions/components/RoleSelector";
import ApiKeyCreatedModal from "./ApiKeyCreatedModal";
import { useCreateApiKey, useRepositoryRoles } from "@scm-manager/ui-api";
import { ApiKeysCollection, Me, User } from "@scm-manager/ui-types";

type Props = {
  user: User | Me;
  apiKeys: ApiKeysCollection;
};

const AddApiKey: FC<Props> = ({ user, apiKeys }) => {
  const [t] = useTranslation("users");
  const {
    isPending: isCurrentlyAddingKey,
    error: errorAddingKey,
    apiKey: addedKey,
    create,
    reset: resetCreationHook,
  } = useCreateApiKey(user, apiKeys);
  const [displayName, setDisplayName] = useState("");
  const [permissionRole, setPermissionRole] = useState("");
  const {
    isLoading: isLoadingRepositoryRoles,
    data: availableRepositoryRoles,
    error: errorLoadingRepositoryRoles,
  } = useRepositoryRoles();

  const isValid = () => {
    return !!displayName && !!permissionRole;
  };

  const resetForm = () => {
    setDisplayName("");
    setPermissionRole("");
  };

  if (errorLoadingRepositoryRoles) {
    return <ErrorNotification error={errorLoadingRepositoryRoles} />;
  }

  if (isLoadingRepositoryRoles) {
    return <Loading />;
  }

  const availableRoleNames = availableRepositoryRoles
    ? availableRepositoryRoles._embedded?.repositoryRoles.map((r) => r.name)
    : [];

  const closeModal = () => {
    resetForm();
    resetCreationHook();
  };

  const newKeyModal = addedKey && <ApiKeyCreatedModal addedKey={addedKey} close={closeModal} />;

  return (
    <>
      <hr />
      {errorAddingKey ? <ErrorNotification error={errorAddingKey} /> : null}
      <Subtitle subtitle={t("apiKey.addSubtitle")} />
      {newKeyModal}
      <InputField label={t("apiKey.displayName")} value={displayName} onChange={setDisplayName} />
      <RoleSelector
        loading={!availableRoleNames}
        availableRoles={availableRoleNames}
        label={t("apiKey.permissionRole.label")}
        helpText={t("apiKey.permissionRole.help")}
        handleRoleChange={setPermissionRole}
        role={permissionRole}
      />
      <Level
        right={
          <Button
            variant="primary"
            isLoading={isCurrentlyAddingKey}
            disabled={!isValid() || isCurrentlyAddingKey}
            testId="submit-button"
            onClick={() => {
              create({ displayName, permissionRole });
              window.scrollTo(0, 0);
            }}
          >
            {t("apiKey.addKey")}
          </Button>
        }
      />
    </>
  );
};

export default AddApiKey;

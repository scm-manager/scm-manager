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
import { ErrorNotification, InputField, Level, Loading, SubmitButton, Subtitle } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import RoleSelector from "../../../repos/permissions/components/RoleSelector";
import ApiKeyCreatedModal from "./ApiKeyCreatedModal";
import { useCreateApiKey, useRepositoryRoles } from "@scm-manager/ui-api";
import { ApiKeysCollection, Me, User } from "@scm-manager/ui-types";

type Props = {
  user: User | Me;
  apiKeys: ApiKeysCollection;
  refresh: () => void;
};

const AddApiKey: FC<Props> = ({ user, apiKeys, refresh }) => {
  const [t] = useTranslation("users");
  const {
    isLoading: isCurrentlyAddingKey,
    error: errorAddingKey,
    apiKey: addedKey,
    create,
    reset: resetCreationHook
  } = useCreateApiKey(user, apiKeys);
  const [displayName, setDisplayName] = useState("");
  const [permissionRole, setPermissionRole] = useState("");
  const {
    isLoading: isLoadingRepositoryRoles,
    data: availableRepositoryRoles,
    error: errorLoadingRepositoryRoles,
  } = useRepositoryRoles();
  const loading = isCurrentlyAddingKey || isLoadingRepositoryRoles;
  const error = errorAddingKey || errorLoadingRepositoryRoles;

  const isValid = () => {
    return !!displayName && !!permissionRole;
  };

  const resetForm = () => {
    setDisplayName("");
    setPermissionRole("");
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  const availableRoleNames = availableRepositoryRoles
    ? availableRepositoryRoles._embedded.repositoryRoles.map((r) => r.name)
    : [];

  const closeModal = () => {
    resetForm();
    refresh();
    resetCreationHook();
  };

  const newKeyModal = addedKey && <ApiKeyCreatedModal addedKey={addedKey} close={closeModal} />;

  return (
    <>
      <hr />
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
          <SubmitButton
            label={t("apiKey.addKey")}
            loading={loading}
            disabled={!isValid()}
            action={() => create({ displayName, permissionRole })}
          />
        }
      />
    </>
  );
};

export default AddApiKey;

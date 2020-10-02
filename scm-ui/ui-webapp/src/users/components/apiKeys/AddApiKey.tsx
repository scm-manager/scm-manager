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

import React, { FC, useEffect, useState } from "react";
import { apiClient, ErrorNotification, InputField, Level, Loading, SubmitButton } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { CONTENT_TYPE_API_KEY } from "./SetApiKeys";
import { connect } from "react-redux";
import {
  fetchAvailablePermissionsIfNeeded,
  getAvailableRepositoryRoles
} from "../../../repos/permissions/modules/permissions";
import { RepositoryRole } from "@scm-manager/ui-types";
import { getRepositoryRolesLink, getRepositoryVerbsLink } from "../../../modules/indexResource";
import RoleSelector from "../../../repos/permissions/components/RoleSelector";
import ApiKeyCreatedModal from "./ApiKeyCreatedModal";

type Props = {
  createLink: string;
  refresh: () => void;
  repositoryRolesLink: string;
  repositoryVerbsLink: string;
  fetchAvailablePermissionsIfNeeded: (repositoryRolesLink: string, repositoryVerbsLink: string) => void;
  availableRepositoryRoles?: RepositoryRole[];
};

const AddApiKey: FC<Props> = ({
  createLink,
  refresh,
  fetchAvailablePermissionsIfNeeded,
  repositoryRolesLink,
  repositoryVerbsLink,
  availableRepositoryRoles
}) => {
  const [t] = useTranslation("users");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<undefined | Error>();
  const [displayName, setDisplayName] = useState("");
  const [permissionRole, setPermissionRole] = useState("");
  const [addedKey, setAddedKey] = useState("");

  useEffect(() => {
    if (!availableRepositoryRoles) {
      fetchAvailablePermissionsIfNeeded(repositoryRolesLink, repositoryVerbsLink);
    }
  });

  const isValid = () => {
    return !!displayName && !!permissionRole;
  };

  const resetForm = () => {
    setDisplayName("");
    setPermissionRole("");
  };

  const addKey = () => {
    setLoading(true);
    apiClient
      .post(createLink, { displayName: displayName, permissionRole: permissionRole }, CONTENT_TYPE_API_KEY)
      .then(response => response.text())
      .then(setAddedKey)
      .then(() => setLoading(false))
      .catch(setError);
  };

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (loading) {
    return <Loading />;
  }

  const availableRoleNames = availableRepositoryRoles ? availableRepositoryRoles.map(r => r.name) : [];

  const closeModal = () => {
    resetForm();
    refresh();
    setAddedKey("");
  };

  const newKeyModal = addedKey && <ApiKeyCreatedModal addedKey={addedKey} close={closeModal} />;

  return (
    <>
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
        right={<SubmitButton label={t("apiKey.addKey")} loading={loading} disabled={!isValid()} action={addKey} />}
      />
    </>
  );
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const availableRepositoryRoles = getAvailableRepositoryRoles(state);
  const repositoryRolesLink = getRepositoryRolesLink(state);
  const repositoryVerbsLink = getRepositoryVerbsLink(state);

  return {
    availableRepositoryRoles,
    repositoryRolesLink,
    repositoryVerbsLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchAvailablePermissionsIfNeeded: (repositoryRolesLink: string, repositoryVerbsLink: string) => {
      dispatch(fetchAvailablePermissionsIfNeeded(repositoryRolesLink, repositoryVerbsLink));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(AddApiKey);

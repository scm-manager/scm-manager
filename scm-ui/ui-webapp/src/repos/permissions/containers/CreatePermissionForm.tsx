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
import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
  Link,
  Namespace,
  PermissionCollection,
  PermissionCreateEntry,
  Repository,
  RepositoryRole,
  SelectValue,
} from "@scm-manager/ui-types";
import {
  Button,
  ErrorNotification,
  GroupAutocomplete,
  LabelWithHelpIcon,
  Level,
  Radio,
  SubmitButton,
  Subtitle,
  UserAutocomplete,
} from "@scm-manager/ui-components";
import * as validator from "../utils/permissionValidation";
import RoleSelector from "../components/RoleSelector";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";
import { useCreatePermission, useIndexLinks } from "@scm-manager/ui-api";
import findVerbsForRole from "../utils/findVerbsForRole";

type Props = {
  availableRoles: RepositoryRole[];
  availableVerbs: string[];
  currentPermissions: PermissionCollection;
  namespaceOrRepository: Namespace | Repository;
};

type PermissionState = PermissionCreateEntry & {
  valid: boolean;
  value?: SelectValue;
};

const useAutoCompleteLinks = () => {
  const links = useIndexLinks()?.autocomplete as Link[];
  return {
    groups: links?.find((l) => l.name === "groups"),
    users: links?.find((l) => l.name === "users"),
  };
};

const CreatePermissionForm: FC<Props> = ({
  availableRoles,
  availableVerbs,
  currentPermissions,
  namespaceOrRepository,
}) => {
  const initialPermissionState = {
    name: "",
    role: "READ",
    verbs: [],
    groupPermission: false,
    valid: false,
  };
  const links = useAutoCompleteLinks();
  const { isLoading, error, create, permission: createdPermission } = useCreatePermission(namespaceOrRepository);
  const [showAdvancedDialog, setShowAdvancedDialog] = useState(false);
  const [permission, setPermission] = useState<PermissionState>(initialPermissionState);
  const [t] = useTranslation("repos");
  useEffect(() => {
    setPermission({
      ...initialPermissionState,
      groupPermission: createdPermission ? createdPermission.groupPermission : initialPermissionState.groupPermission,
      role: createdPermission ? createdPermission.role : initialPermissionState.role,
      verbs: createdPermission ? createdPermission?.verbs : initialPermissionState.verbs,
    });
    //eslint-disable-next-line
  }, [createdPermission]);
  const selectedVerbs = permission.role ? findVerbsForRole(availableRoles, permission.role) : permission.verbs;

  const selectName = (value: SelectValue) => {
    setPermission({
      ...permission,
      value,
      name: value.value.id,
      valid: validator.isPermissionValid(
        value.value.id,
        permission.groupPermission,
        currentPermissions._embedded?.permissions || []
      ),
    });
  };

  const groupPermissionScopeChanged = (value: boolean) => {
    if (value) {
      permissionScopeChanged(true);
    }
  };

  const userPermissionScopeChanged = (value: boolean) => {
    if (value) {
      permissionScopeChanged(false);
    }
  };

  const permissionScopeChanged = (groupPermission: boolean) => {
    setPermission({
      ...permission,
      groupPermission,
    });
  };

  const handleRoleChange = (role: string) => {
    const selectedRole = findAvailableRole(role);
    if (!selectedRole) {
      return;
    }
    setPermission({
      ...permission,
      verbs: [],
      role,
    });
  };

  const submitAdvancedPermissionsDialog = (verbs: string[]) => {
    setPermission({
      ...permission,
      role: undefined,
      verbs,
    });
    setShowAdvancedDialog(false);
  };

  const submit = (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    create(permission);
  };

  const findAvailableRole = (roleName: string) => {
    return availableRoles.find((role) => role.name === roleName);
  };

  const empty = { value: { id: "", displayName: "" }, label: "" };

  return (
    <>
      <hr />
      <Subtitle subtitle={t("permission.add-permission.add-permission-heading")} />
      {showAdvancedDialog ? (
        <AdvancedPermissionsDialog
          availableVerbs={availableVerbs}
          selectedVerbs={selectedVerbs || []}
          onClose={() => setShowAdvancedDialog(false)}
          onSubmit={submitAdvancedPermissionsDialog}
          readOnly={!create}
        />
      ) : null}
      <ErrorNotification error={error} />
      <form onSubmit={submit}>
        <div className="field is-grouped is-grouped-multiline">
          <div className="control is-flex-shrink-1">
            <Radio
              name="permission_scope"
              value="USER_PERMISSION"
              checked={!permission.groupPermission}
              label={t("permission.user-permission")}
              onChange={userPermissionScopeChanged}
            />
            <Radio
              name="permission_scope"
              value="GROUP_PERMISSION"
              checked={permission.groupPermission}
              label={t("permission.group-permission")}
              onChange={groupPermissionScopeChanged}
            />
          </div>
        </div>
        <div className="columns">
          <div className="column is-half">
            {permission.groupPermission && links.groups ? (
              <GroupAutocomplete
                autocompleteLink={links.groups.href}
                valueSelected={selectName}
                value={permission.value || empty}
              />
            ) : null}
            {!permission.groupPermission && links.users ? (
              <UserAutocomplete
                autocompleteLink={links.users.href}
                valueSelected={selectName}
                value={permission.value || empty}
              />
            ) : null}
          </div>
          <div className="column is-half">
            <div className="columns">
              <div className="column is-narrow">
                <RoleSelector
                  availableRoles={availableRoles.map((r) => r.name)}
                  label={t("permission.role")}
                  helpText={t("permission.help.roleHelpText")}
                  handleRoleChange={handleRoleChange}
                  role={permission.role || ""}
                  emptyLabel={t("permission.custom")}
                />
              </div>
              <div className="column">
                <LabelWithHelpIcon
                  label={t("permission.permissions")}
                  helpText={t("permission.help.permissionsHelpText")}
                />
                <Button label={t("permission.advanced-button.label")} action={() => setShowAdvancedDialog(true)} />
              </div>
            </div>
          </div>
        </div>
        <Level
          right={
            <SubmitButton
              label={t("permission.add-permission.submit-button")}
              loading={isLoading}
              disabled={!permission.valid || permission.name === ""}
            />
          }
        />
      </form>
    </>
  );
};

export default CreatePermissionForm;

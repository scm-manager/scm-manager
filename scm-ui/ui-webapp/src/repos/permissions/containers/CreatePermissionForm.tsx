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

import React, { FC, FormEvent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { useCreatePermission, useGroupOptions, useIndexLinks, useUserOptions } from "@scm-manager/ui-api";
import { Help } from "@scm-manager/ui-components";
import {
  Button,
  ComboboxField,
  ErrorNotification,
  Field,
  Label,
  Level,
  RadioGroup,
  Subtitle,
} from "@scm-manager/ui-core";
import {
  AutocompleteObject,
  Link,
  Namespace,
  Option,
  PermissionCollection,
  PermissionCreateEntry,
  Repository,
  RepositoryRole,
} from "@scm-manager/ui-types";
import RoleSelector from "../components/RoleSelector";
import findVerbsForRole from "../utils/findVerbsForRole";
import * as validator from "../utils/permissionValidation";
import AdvancedPermissionsDialog from "./AdvancedPermissionsDialog";

type Props = {
  availableRoles: RepositoryRole[];
  availableVerbs: string[];
  currentPermissions: PermissionCollection;
  namespaceOrRepository: Namespace | Repository;
};

type PermissionState = PermissionCreateEntry & {
  valid: boolean;
  value?: Option<AutocompleteObject>;
};

const useAutoCompleteLinks = () => {
  const links = useIndexLinks()?.autocomplete as Link[];
  return {
    groups: links?.find((l) => l.name === "groups"),
    users: links?.find((l) => l.name === "users"),
  };
};

const isRepository = (namespaceOrRepository: Namespace | Repository): namespaceOrRepository is Repository => {
  return (namespaceOrRepository as Repository).name !== undefined;
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
  const { isPending, error, create, permission: createdPermission } = useCreatePermission(namespaceOrRepository);
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
  const [userQuery, setUserQuery] = useState("");
  const [groupQuery, setGroupQuery] = useState("");
  const { data: userOptions, isLoading: userOptionsLoading } = useUserOptions(userQuery);
  const { data: groupOptions, isLoading: groupOptionsLoading } = useGroupOptions(groupQuery);

  const selectName = (value?: Option<AutocompleteObject>) => {
    if (value) {
      setPermission((prevState) => ({
        ...prevState,
        value,
        name: value.value.id,
        valid: validator.isPermissionValid(
          value.value.id,
          permission.groupPermission,
          currentPermissions._embedded?.permissions || [],
        ),
      }));
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
          entityType={isRepository(namespaceOrRepository) ? "repository" : "namespace"}
        />
      ) : null}
      <ErrorNotification error={error} />
      <form onSubmit={submit}>
        <Field>
          <RadioGroup
            name="permission_scope"
            className="is-flex is-flex-wrap-wrap"
            value={permission.groupPermission ? "GROUP_PERMISSION" : "USER_PERMISSION"}
            onValueChange={(value) => permissionScopeChanged(value === "GROUP_PERMISSION")}
          >
            <div className="mr-3">
              <RadioGroup.Option value="USER_PERMISSION" label={t("permission.user-permission")} />
            </div>
            <div className="mr-3">
              <RadioGroup.Option value="GROUP_PERMISSION" label={t("permission.group-permission")} />
            </div>
          </RadioGroup>
        </Field>
        <div className="columns">
          <div className="column is-half">
            {permission.groupPermission && links.groups ? (
              <ComboboxField<AutocompleteObject>
                label={t("permission.group-select")}
                options={groupOptions || []}
                onChange={selectName}
                onQueryChange={setGroupQuery}
                value={permission.value || empty}
                className={classNames({ "is-loading": groupOptionsLoading })}
              />
            ) : null}
            {!permission.groupPermission && links.users ? (
              <ComboboxField<AutocompleteObject>
                label={t("permission.user-select")}
                options={userOptions || []}
                onQueryChange={setUserQuery}
                onChange={selectName}
                value={permission.value || empty}
                className={classNames({ "is-loading": userOptionsLoading })}
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
                <Label>
                  {t("permission.permissions")}
                  <Help message={t("permission.help.permissionsHelpText")} />
                </Label>
                <Button onClick={() => setShowAdvancedDialog(true)}>{t("permission.advanced-button.label")}</Button>
              </div>
            </div>
          </div>
        </div>
        <Level
          right={
            <Button
              type="submit"
              variant="primary"
              isLoading={isPending}
              disabled={!permission.valid || permission.name === ""}
              testId="submit-button"
              onClick={() => window.scrollTo(0, 0)}
            >
              {t("permission.add-permission.submit-button")}
            </Button>
          }
        />
      </form>
    </>
  );
};

export default CreatePermissionForm;

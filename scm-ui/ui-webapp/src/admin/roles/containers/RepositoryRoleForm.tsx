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

import React, { FC, FormEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { RepositoryRole } from "@scm-manager/ui-types";
import { ErrorNotification, InputField, Level, SubmitButton } from "@scm-manager/ui-components";
import PermissionsWrapper from "../../../permissions/components/PermissionsWrapper";
import { useRepositoryVerbs, useRequiredIndexLink } from "@scm-manager/ui-api";

type Props = {
  role?: RepositoryRole;
  submitForm: (p: RepositoryRole) => void;
};

const RepositoryRoleForm: FC<Props> = ({ role: initialRole, submitForm }) => {
  const { isLoading: loading, error, data } = useRepositoryVerbs();
  const createLink = useRequiredIndexLink("repositoryRoles");
  const [t] = useTranslation("admin");
  const [role, setRole] = useState<RepositoryRole>(
    initialRole || {
      name: "",
      verbs: [],
      _links: {
        create: { href: createLink }
      }
    }
  );
  const availableVerbs = data?.verbs;

  if (error) {
    return <ErrorNotification error={error} />;
  }

  const isValid = !!role?.name && role.verbs.length > 0;
  const handleNameChange = (name: string) => setRole({ ...role, name });

  const handleVerbChange = (value: boolean, name: string) =>
    setRole({
      ...role,
      verbs: value ? [...role.verbs, name] : role.verbs.filter(v => v !== name)
    });

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid) {
      submitForm(role);
    }
  };

  const selectedVerbs =
    availableVerbs?.reduce((obj, verb) => ({ ...obj, [verb]: role?.verbs.includes(verb) }), {}) || {};

  return (
    <form onSubmit={submit}>
      <InputField
        name="name"
        label={t("repositoryRole.form.name")}
        onChange={handleNameChange}
        value={role.name || ""}
        disabled={!!initialRole}
      />
      <div className="field">
        <label className="label">{t("repositoryRole.form.permissions")}</label>
        <PermissionsWrapper permissions={selectedVerbs} onChange={handleVerbChange} disabled={false} role={true} />
      </div>
      <hr />
      <Level right={<SubmitButton loading={loading} label={t("repositoryRole.form.submit")} disabled={!isValid} />} />
    </form>
  );
};

export default RepositoryRoleForm;

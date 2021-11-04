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
        create: { href: createLink },
      },
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
      verbs: value ? [...role.verbs, name] : role.verbs.filter((v) => v !== name),
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

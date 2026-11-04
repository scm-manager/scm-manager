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

import React, { FC, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Controller } from "react-hook-form";
import { RepositoryRole, RepositoryRoleCreation } from "@scm-manager/ui-types";
import { ErrorNotification, Field, Form, Label, useScmFormContext } from "@scm-manager/ui-core";
import { useRepositoryVerbs } from "@scm-manager/ui-api";
import PermissionsWrapper from "../../../permissions/components/PermissionsWrapper";

type Props = {
  submitForm: (role: RepositoryRoleCreation) => Promise<unknown>;
  role?: RepositoryRole;
};

const VerbsField: FC<{ availableVerbs: string[] }> = ({ availableVerbs }) => {
  const { control, readOnly } = useScmFormContext();
  return (
    <Controller
      control={control}
      name="verbs"
      rules={{ validate: (verbs: string[]) => verbs.length > 0 }}
      render={({ field: { value, onChange } }) => (
        <PermissionsWrapper
          permissions={availableVerbs.reduce((obj, verb) => ({ ...obj, [verb]: value.includes(verb) }), {})}
          onChange={(checked, verb) =>
            onChange(checked ? [...value, verb] : value.filter((selected: string) => selected !== verb))
          }
          disabled={!!readOnly}
          role={true}
        />
      )}
    />
  );
};

const RepositoryRoleForm: FC<Props> = ({ role, submitForm }) => {
  const { error, data } = useRepositoryVerbs();
  const [t] = useTranslation("admin");
  const defaultValues = useMemo<RepositoryRoleCreation>(
    () => ({ name: role?.name ?? "", verbs: role?.verbs ?? [] }),
    [role],
  );

  if (error) {
    return <ErrorNotification error={error} />;
  }

  return (
    <Form translationPath={["admin", "repositoryRole.form"]} defaultValues={defaultValues} onSubmit={submitForm}>
      <Form.Row>
        <Form.Input
          name="name"
          label={t("repositoryRole.form.name")}
          rules={{ required: true }}
          // undefined keeps the form's own read only state while it is submitting
          readOnly={role ? true : undefined}
          autoFocus={!role}
        />
      </Form.Row>
      <Field as="fieldset">
        <Label as="legend">{t("repositoryRole.form.permissions")}</Label>
        <VerbsField availableVerbs={data?.verbs ?? []} />
      </Field>
      <hr />
    </Form>
  );
};

export default RepositoryRoleForm;

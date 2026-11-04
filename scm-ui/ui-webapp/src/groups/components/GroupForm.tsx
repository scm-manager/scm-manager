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

import React, { FC, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useUserOptions } from "@scm-manager/ui-api";
import { Combobox, Form } from "@scm-manager/ui-core";
import { AutocompleteObject, GroupCreation } from "@scm-manager/ui-types";
import * as validator from "./groupValidation";

type GroupFormValues = {
  name: string;
  description: string;
  type: string;
  external: boolean;
  members: AutocompleteObject[];
};

type Props = {
  submitForm: (group: GroupCreation) => Promise<unknown>;
  group?: GroupCreation;
  transmittedName?: string;
  transmittedExternal?: boolean;
  successMessage?: string;
};

const toMember = (name: string): AutocompleteObject => ({ id: name, displayName: name });

const GroupForm: FC<Props> = ({
  submitForm,
  group,
  transmittedName = "",
  transmittedExternal = false,
  successMessage,
}) => {
  const [t] = useTranslation("groups");
  const [query, setQuery] = useState("");
  const { data: userOptions, isLoading: userOptionsLoading } = useUserOptions(query);
  const defaultValues = useMemo<GroupFormValues>(
    () => ({
      name: group?.name ?? transmittedName,
      description: group?.description ?? "",
      type: group?.type ?? "",
      external: group?.external ?? transmittedExternal,
      members: group?.members.map(toMember) ?? [],
    }),
    [group, transmittedName, transmittedExternal],
  );

  const submit = ({ members, ...values }: GroupFormValues) =>
    submitForm({ ...values, members: values.external ? [] : members.map((member) => member.id) });

  return (
    <Form
      translationPath={["groups", "groupForm"]}
      defaultValues={defaultValues}
      onSubmit={submit}
      successMessageFallback={successMessage}
      allowSubmitWithoutChanges={!group}
    >
      {({ watch }) => (
        <>
          {!group ? (
            <Form.Row>
              <Form.Input
                name="name"
                label={t("group.name")}
                rules={{
                  required: t("groupForm.nameError"),
                  validate: (name: string) => validator.isNameValid(name) || t("groupForm.nameError"),
                }}
                autoFocus
              />
            </Form.Row>
          ) : null}
          <Form.Row>
            <Form.Textarea name="description" label={t("group.description")} />
          </Form.Row>
          <Form.Row>
            <Form.Checkbox
              name="external"
              label={t("group.external")}
              helpText={t("groupForm.help.externalHelpText")}
            />
          </Form.Row>
          <Form.Row hidden={watch("external")}>
            <Form.ChipInput
              name="members"
              label={t("groupForm.addMemberAutocomplete.buttonLabel")}
              aria-label={t("groupForm.addMemberAutocomplete.buttonLabel")}
              placeholder={t("groupForm.addMemberAutocomplete.placeholder")}
              optionFactory={(member: AutocompleteObject) => ({ label: member.id, value: member })}
              isNewItemDuplicate={(existing, added) =>
                (existing.value as AutocompleteObject).id === (added.value as AutocompleteObject).id
              }
              isLoading={userOptionsLoading}
            >
              <Combobox<AutocompleteObject> options={userOptions || []} onQueryChange={setQuery} />
            </Form.ChipInput>
          </Form.Row>
        </>
      )}
    </Form>
  );
};

export default GroupForm;

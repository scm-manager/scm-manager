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
import { AutocompleteObject, Group, Member, Option } from "@scm-manager/ui-types";
import { Checkbox, InputField, Level, SubmitButton, Subtitle, Textarea } from "@scm-manager/ui-components";
import * as validator from "./groupValidation";
import { ChipInputField, Combobox } from "@scm-manager/ui-forms";
import { useUserOptions } from "@scm-manager/ui-api";

type Props = {
  submitForm: (p: Group) => void;
  loading?: boolean;
  group?: Group;
  transmittedName?: string;
  transmittedExternal?: boolean;
};

const GroupForm: FC<Props> = ({ submitForm, loading, group, transmittedName = "", transmittedExternal = false }) => {
  const [t] = useTranslation("groups");
  const [groupState, setGroupState] = useState({
    name: transmittedName,
    description: "",
    _embedded: {
      members: [] as Member[],
    },
    _links: {},
    members: [] as string[],
    type: "",
    external: transmittedExternal,
  });
  const [nameValidationError, setNameValidationError] = useState(false);
  const [query, setQuery] = useState("");
  const { data: userOptions, isLoading: userOptionsLoading } = useUserOptions(query);

  useEffect(() => {
    if (group) {
      setGroupState(group);
    }
  }, [group]);

  const isValid = !(nameValidationError || !groupState.name);

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid) {
      if (groupState.external) {
        groupState.members = [];
      }
      submitForm(groupState);
    }
  };

  const renderMemberfields = () => {
    if (groupState.external) {
      return null;
    }

    return (
      <ChipInputField<AutocompleteObject>
        label={t("groupForm.addMemberAutocomplete.buttonLabel")}
        aria-label="groupForm.addMemberAutocomplete.ariaLabel"
        placeholder={t("groupForm.addMemberAutocomplete.placeholder")}
        value={groupState.members.map((m) => ({ label: m, value: { id: m, displayName: m } }))}
        onChange={updateMembers}
        isLoading={userOptionsLoading}
        isNewItemDuplicate={(prev, cur) => prev.value.id === cur.value.id}
      >
        <Combobox<AutocompleteObject> options={userOptions || []} onQueryChange={setQuery} />
      </ChipInputField>
    );
  };

  const renderExternalField = () => {
    if (!groupState) {
      return null;
    }
    return (
      <Checkbox
        label={t("group.external")}
        checked={groupState.external}
        helpText={t("groupForm.help.externalHelpText")}
        onChange={(external) => setGroupState({ ...groupState, external })}
      />
    );
  };

  const updateMembers = (members: Array<Option<AutocompleteObject>>) => {
    setGroupState((prevState) => ({ ...prevState, members: members.map((m) => m.value.id) }));
  };

  let nameField = null;
  let subtitle = null;
  if (!group) {
    // create new group
    nameField = (
      <InputField
        label={t("group.name")}
        errorMessage={t("groupForm.nameError")}
        onChange={(name) => {
          setNameValidationError(!validator.isNameValid(name));
          setGroupState({ ...groupState, name });
        }}
        value={groupState.name}
        validationError={nameValidationError}
        helpText={t("groupForm.help.nameHelpText")}
      />
    );
  } else if (group.external) {
    subtitle = <Subtitle subtitle={t("groupForm.externalSubtitle")} />;
  } else {
    subtitle = <Subtitle subtitle={t("groupForm.subtitle")} />;
  }

  return (
    <>
      {subtitle}
      <form onSubmit={submit}>
        {nameField}
        <Textarea
          label={t("group.description")}
          errorMessage={t("groupForm.descriptionError")}
          onChange={(description) => setGroupState({ ...groupState, description })}
          value={groupState.description}
          validationError={false}
          helpText={t("groupForm.help.descriptionHelpText")}
        />
        {renderExternalField()}
        {renderMemberfields()}
        <Level right={<SubmitButton disabled={!isValid} label={t("groupForm.submit")} loading={loading} />} />
      </form>
    </>
  );
};

export default GroupForm;

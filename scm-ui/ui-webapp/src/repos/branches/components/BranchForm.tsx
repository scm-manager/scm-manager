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
import { Branch, BranchCreation } from "@scm-manager/ui-types";
import { validation as validator } from "@scm-manager/ui-components";
import { Button, InputField, Level, SelectField } from "@scm-manager/ui-core";
import { orderBranches } from "../util/orderBranches";

type Props = {
  submitForm: (p: BranchCreation) => void;
  branches: Branch[];
  loading?: boolean;
  transmittedName?: string;
  disabled?: boolean;
};

const autoFocus = (el: HTMLSelectElement) => el?.focus();

const BranchForm: FC<Props> = ({ submitForm, branches, disabled, transmittedName, loading }) => {
  const [t] = useTranslation("repos");
  const [name, setName] = useState(transmittedName || "");
  const [source, setSource] = useState<string>();
  const [nameValid, setNameValid] = useState(true);

  orderBranches(branches);
  const options = branches.map((branch) => ({
    label: branch.name,
    value: branch.name,
  }));
  // the select shows the first branch as long as the user has not chosen another one
  const selectedSource = source ?? options[0]?.value ?? "";

  const changeName = (value: string) => {
    setName(value);
    setNameValid(validator.isBranchValid(value));
  };

  const isValid = () => nameValid && selectedSource && name;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid()) {
      submitForm({
        name,
        parent: selectedSource,
      });
    }
  };

  return (
    <>
      <form onSubmit={submit}>
        <div className="columns">
          <div className="column">
            <SelectField
              name="source"
              label={t("branches.create.source")}
              options={options}
              value={selectedSource}
              onChange={(e) => setSource(e.target.value)}
              disabled={disabled}
              ref={autoFocus}
            />
            <InputField
              name="name"
              label={t("branches.create.name")}
              onChange={(event) => changeName(event.target.value)}
              value={name ? name : ""}
              error={!nameValid ? t("validation.branch.nameInvalid") : undefined}
              disabled={!!transmittedName || disabled}
              testId="input-branch-name"
              required
            />
          </div>
        </div>
        <div className="columns">
          <div className="column">
            <Level
              right={
                <Button
                  type="submit"
                  variant="primary"
                  disabled={disabled || !isValid()}
                  isLoading={loading}
                  testId="submit-button"
                  onClick={() => window.scrollTo(0, 0)}
                >
                  {t("branches.create.submit")}
                </Button>
              }
            />
          </div>
        </div>
      </form>
    </>
  );
};

export default BranchForm;

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
import { Branch, BranchCreation } from "@scm-manager/ui-types";
import { InputField, Level, Select, SubmitButton, validation as validator } from "@scm-manager/ui-components";
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
  const [source, setSource] = useState("");
  const [nameValid, setNameValid] = useState(false);

  useEffect(() => {
    setNameValid(validator.isBranchValid(name));
  }, [name]);

  const isValid = () => nameValid && source && name;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (isValid()) {
      submitForm({
        name,
        parent: source,
      });
    }
  };

  orderBranches(branches);
  const options = branches.map((branch) => ({
    label: branch.name,
    value: branch.name,
  }));

  return (
    <>
      <form onSubmit={submit}>
        <div className="columns">
          <div className="column">
            <Select
              name="source"
              label={t("branches.create.source")}
              options={options}
              onChange={(e) => setSource(e.target.value)}
              loading={loading}
              disabled={disabled}
              ref={autoFocus}
            />
            <InputField
              name="name"
              label={t("branches.create.name")}
              onChange={setName}
              value={name ? name : ""}
              validationError={!nameValid}
              errorMessage={t("validation.branch.nameInvalid")}
              disabled={!!transmittedName || disabled}
              testId="input-branch-name"
            />
          </div>
        </div>
        <div className="columns">
          <div className="column">
            <Level
              right={
                <SubmitButton disabled={disabled || !isValid()} loading={loading} label={t("branches.create.submit")} />
              }
            />
          </div>
        </div>
      </form>
    </>
  );
};

export default BranchForm;

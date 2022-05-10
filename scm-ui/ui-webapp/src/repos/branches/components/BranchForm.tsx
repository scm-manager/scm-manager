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
              onChange={setSource}
              loading={loading}
              disabled={disabled}
            />
            <InputField
              name="name"
              label={t("branches.create.name")}
              onChange={setName}
              value={name ? name : ""}
              validationError={!nameValid}
              errorMessage={t("validation.branch.nameInvalid")}
              disabled={!!transmittedName || disabled}
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

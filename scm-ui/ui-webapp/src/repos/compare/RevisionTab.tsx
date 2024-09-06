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

import { CompareFunction, CompareProps } from "./CompareSelectBar";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-core";
import { Input } from "@scm-manager/ui-forms";
import { SubmitHandler, useForm } from "react-hook-form";

type Props = {
  selected: CompareProps;
  onSelect: CompareFunction;
};

type FormProps = {
  revision: string;
};

const SizedForm = styled.form`
  width: 18.5rem;
`;

const SmallButton = styled(Button)`
  height: 1.875rem;
`;

const RevisionTab: FC<Props> = ({ selected, onSelect }) => {
  const [t] = useTranslation("repos");
  const defaultValue = selected.type === "r" ? selected.name : "";
  const { register, handleSubmit, watch } = useForm<FormProps>({
    defaultValues: {
      revision: defaultValue,
    },
  });
  const onSubmit: SubmitHandler<FormProps> = (data) => onSelect("r", data.revision);

  return (
    <SizedForm className="mt-2" onSubmit={handleSubmit(onSubmit)}>
      <div className="field has-addons is-justify-content-center">
        <div className="control">
          <Input
            className="is-small"
            placeholder={t("compare.selector.revision.input")}
            {...register("revision", { required: true })}
          />
        </div>
        <div className="control">
          <SmallButton className="is-info is-small" type="submit" disabled={!watch("revision")}>
            {t("compare.selector.revision.submit")}
          </SmallButton>
        </div>
      </div>
    </SizedForm>
  );
};

export default RevisionTab;

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

import { CompareFunction, CompareProps } from "./CompareSelectBar";
import React, { FC } from "react";
import { useTranslation } from "react-i18next";
import styled from "styled-components";
import { Button } from "@scm-manager/ui-buttons";
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

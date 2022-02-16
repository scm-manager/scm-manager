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
import React, { FC } from "react";
import { Changeset, File, Repository } from "@scm-manager/ui-types";
import { Button, ButtonAddons } from "../../buttons";
import { createChangesetLink, createSourcesLink } from "./changesets";
import { useTranslation } from "react-i18next";

type Props = {
  repository: Repository;
  changeset: Changeset;
  file?: File;
};

const ChangesetButtonGroup: FC<Props> = ({ repository, changeset, file }) => {
  const [t] = useTranslation("repos");
  const changesetLink = createChangesetLink(repository, changeset);
  const sourcesLink = createSourcesLink(repository, changeset, file);
  return (
    <ButtonAddons className="m-0">
      <Button link={changesetLink} icon="exchange-alt" label={t("changeset.buttons.details")} reducedMobile={true} />
      <Button link={sourcesLink} icon="code" label={t("changeset.buttons.sources")} reducedMobile={true} />
    </ButtonAddons>
  );
};

export default ChangesetButtonGroup;

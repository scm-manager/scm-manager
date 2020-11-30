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

import React, { FC, useState } from "react";
import { RepositoryType, Link } from "@scm-manager/ui-types";

import { useTranslation } from "react-i18next";
import ImportRepositoryTypeSelect from "../components/ImportRepositoryTypeSelect";
import ImportTypeSelect from "../components/ImportTypeSelect";
import RepositoryImportFromUrl from "../components/RepositoryImportFromUrl";

type Props = {
  repositoryTypes: RepositoryType[];
};

const ImportRepository: FC<Props> = ({ repositoryTypes }) => {
  const [repositoryType, setRepositoryType] = useState<RepositoryType | undefined>();
  const [importType, setImportType] = useState("");
  const [t] = useTranslation("repos");

  const changeRepositoryType = (repositoryType: RepositoryType) => {
    setImportType(((repositoryType!._links.import as Link[])[0] as Link).name!);
    setRepositoryType(repositoryType);
  };

  const renderImportComponent = () => {
    if (importType === "url") {
      return (
        <RepositoryImportFromUrl
          url={((repositoryType!._links.import as Link[])!.find((link: Link) => link.name === "url") as Link).href}
        />
      );
    }

    throw new Error("Unknown import type");
  };

  return (
    <div>
      <ImportRepositoryTypeSelect
        repositoryTypes={repositoryTypes}
        repositoryType={repositoryType}
        setRepositoryType={changeRepositoryType}
      />
      {repositoryType && (
        <>
          <hr />
          <ImportTypeSelect repositoryType={repositoryType} importType={importType} setImportType={setImportType} />
        </>
      )}
      {importType && renderImportComponent()}
    </div>
  );
};

export default ImportRepository;

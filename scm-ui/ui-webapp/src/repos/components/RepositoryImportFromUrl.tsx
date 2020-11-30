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
import { InputField } from "@scm-manager/ui-components";
import styled from "styled-components";
import { useTranslation } from "react-i18next";

type Props = {
  url: string;
};

const Column = styled.div`
  padding: 0 0.75rem;
`;

const Columns = styled.div`
  padding: 0.75rem 0 0;
`;

const RepositoryImportFromUrl: FC<Props> = ({}) => {
  const [name, setName] = useState("");
  const [importUrl, setImportUrl] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [t] = useTranslation("repos");

  const handleImportUrlChange = (url: string) => {
    if (!name) {
      // If the repository name is not fill we set a name suggestion
      const match = url.match(/([^\/]+)(\.git)?/i);
      if (match && match[1]) {
        setName(match[1]);
      }
    }
    setImportUrl(url);
  };

  return (
    <>
      <Columns className="columns is-multiline">
        <Column className="column is-full">
          <InputField
            label={t("import.importUrl")}
            onChange={handleImportUrlChange}
            value={importUrl}
            helpText={t("help.importUrlHelpText")}
          />
        </Column>
        <Column className="column is-half">
          <InputField
            label={t("import.username")}
            onChange={setUsername}
            value={username}
            helpText={t("help.usernameHelpText")}
          />
        </Column>
        <Column className="column is-half">
          <InputField
            label={t("import.password")}
            onChange={setPassword}
            value={password}
            type="password"
            helpText={t("help.passwordHelpText")}
          />
        </Column>
      </Columns>
      <hr />
    </>
  );
};

export default RepositoryImportFromUrl;

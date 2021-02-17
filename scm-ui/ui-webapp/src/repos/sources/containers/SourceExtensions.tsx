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
import { Repository } from "@scm-manager/ui-types";
import { useParams } from "react-router-dom";

import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { useSources } from "@scm-manager/ui-api";

const extensionPointName = "repos.sources.extensions";

type Props = {
  repository: Repository;
  baseUrl: string;
};

type Params = {
  revision: string;
  path: string;
  extension: string;
};

const useUrlParams = () => {
  const { revision, path, extension } = useParams<Params>();
  return {
    revision: revision ? decodeURIComponent(revision) : undefined,
    path: path,
    extension
  };
};

const SourceExtensions: FC<Props> = ({ repository, baseUrl }) => {
  const { revision, path, extension } = useUrlParams();
  const { error, isLoading, data: sources } = useSources(repository, { revision, path });
  const [t] = useTranslation("repos");

  if (error) {
    return <ErrorNotification error={error} />;
  }
  if (isLoading) {
    return <Loading />;
  }

  const extprops = { extension, repository, revision, path, sources, baseUrl };
  if (!binder.hasExtension(extensionPointName, extprops)) {
    return <Notification type="warning">{t("sources.extension.notBound")}</Notification>;
  }

  return <ExtensionPoint name={extensionPointName} props={extprops} />;
};

export default SourceExtensions;

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

import { useEffect } from "react";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import { useTranslation } from "react-i18next";

/**
 * Hook to set the document title.
 *
 * @param titleParts - An array of title parts to be joined.
 *                     Title parts should be sorted with the highest specificity first.
 */
export default function useDocumentTitle(...titleParts: string[]) {
  const [t] = useTranslation("commons");
  useEffect(() => {
    const extension = binder.getExtension<extensionPoints.DocumentTitleExtensionPoint>("document.title");
    let title = [...titleParts, t("documentTitle.suffix")].join(" - ");
    if (extension) {
      if (typeof extension.documentTitle === "string") {
        title += ` (${extension.documentTitle})`;
      } else if (typeof extension.documentTitle === "function") {
        title = extension.documentTitle(title);
      }
    }
    document.title = title;
  }, [titleParts]);
}

/**
 * Hook to set the document title for a repository.
 *
 * @param repository - The repository for which the title should be set.
 * @param titleParts - An array of title parts to be joined.
 *                     Title parts should be sorted with the highest specificity first.
 */
export function useDocumentTitleForRepository(repository: Repository, ...titleParts: string[]) {
  useDocumentTitle(...titleParts, repository.namespace + "/" + repository.name);
}

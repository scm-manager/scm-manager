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

import React, { FC, useMemo } from "react";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { Button } from "@scm-manager/ui-core";
import { SourceViewSelection } from "../../containers/Content";
import { File, Link } from "@scm-manager/ui-types";
import { useContentType } from "@scm-manager/ui-api";

type Props = {
  className?: string;
  selected: SourceViewSelection;
  showSources: () => void;
  showHistory: () => void;
  showAnnotations: () => void;
  file: File;
};

const selectedClassName = (selected: boolean) => (selected ? "is-link is-selected" : undefined);

const FileButtonAddons: FC<Props> = ({ className, selected, showSources, showHistory, showAnnotations, file }) => {
  const [t] = useTranslation("repos");
  const { data: contentTypeData } = useContentType((file._links.self as Link).href);
  const showAnnotationButton = useMemo(
    () =>
      contentTypeData?.language ||
      ["text/", "markdown/"].some((nonBinaryContentType) => contentTypeData?.type.startsWith(nonBinaryContentType)),
    [contentTypeData]
  );

  return (
    <div className={classNames("field", "has-addons", "mb-0", className)}>
      <p className="control">
        <Button
          onClick={showSources}
          className={selectedClassName(selected === "source")}
          title={t("sources.content.sourcesButton")}
        >
          <span className="icon">
            <i className="fas fa-code" />
          </span>
        </Button>
      </p>
      {showAnnotationButton ? (
        <p className="control">
          <Button
            onClick={showAnnotations}
            className={selectedClassName(selected === "annotations")}
            title={t("sources.content.annotateButton")}
          >
            <span className="icon">
              <i className="fas fa-user-clock" />
            </span>
          </Button>
        </p>
      ) : null}
      <p className="control">
        <Button
          onClick={showHistory}
          className={selectedClassName(selected === "history")}
          title={t("sources.content.historyButton")}
        >
          <span className="icon">
            <i className="fas fa-history" />
          </span>
        </Button>
      </p>
    </div>
  );
};

export default FileButtonAddons;

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

import React, { FC } from "react";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";
import classNames from "classnames";
import { urls } from "@scm-manager/ui-components";
import { Button, Icon, LinkButton } from "@scm-manager/ui-core";

type RepositoryForm = {
  path: string;
  icon: string;
  label: string;
};

const RepositoryFormButton: FC<RepositoryForm> = ({ path, icon, label }) => {
  const location = useLocation();
  const href = urls.concat("/repos/create", path);
  const isSelected = href === location.pathname;
  const [t] = useTranslation(["repos", "plugins"]);
  const translatedLabel = t(`plugins:${label}`, label);

  // the gap belongs to the label, so that the icon stays centered when the label is hidden on small screens
  const content = (
    <>
      <Icon className="mr-0">{icon}</Icon>
      <span className={classNames("ml-1", "is-hidden-mobile", "is-hidden-tablet-only")}>{translatedLabel}</span>
    </>
  );

  if (isSelected) {
    return (
      <Button className="is-size-6 is-link is-selected" title={translatedLabel} aria-label={translatedLabel}>
        {content}
      </Button>
    );
  }
  return (
    <LinkButton className="is-size-6" to={href + location.search} title={translatedLabel} aria-label={translatedLabel}>
      {content}
    </LinkButton>
  );
};

type Props = {
  forms: RepositoryForm[];
};

const RepositoryFormSwitcher: FC<Props> = ({ forms }) => (
  <div className="field has-addons mb-0 ml-auto">
    {(forms || []).map((form) => (
      <p className="control" key={form.path}>
        <RepositoryFormButton {...form} />
      </p>
    ))}
  </div>
);

export default RepositoryFormSwitcher;

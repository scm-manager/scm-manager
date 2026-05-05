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

import React, { FC, ReactNode } from "react";
import { useTranslation } from "react-i18next";
import {
  useDocumentTitle,
  useDocumentTitleForRepository,
  useNamespaceFromParams,
  useRepositoryFromParams,
} from "@scm-manager/ui-core";

type GlobalProps = {
  i18nNamespace: string;
  label: string;
  children?: ReactNode;
};

export const TitledGlobalSettingComponent: FC<GlobalProps> = ({ children, i18nNamespace, label }) => {
  const [t] = useTranslation(i18nNamespace);
  const [commonTranslation] = useTranslation("commons");
  useDocumentTitle(t(label), commonTranslation("documentTitle.globalConfiguration"));
  return <>{children}</>;
};

type RepositoryProps = {
  i18nNamespace: string;
  label: string;
  children?: ReactNode;
};

export const TitledRepositorySettingComponent: FC<RepositoryProps> = ({ children, i18nNamespace, label }) => {
  const repository = useRepositoryFromParams();
  const [t] = useTranslation(i18nNamespace);
  const [commonTranslation] = useTranslation("commons");
  useDocumentTitleForRepository(repository, t(label), commonTranslation("documentTitle.repositoryConfiguration"));
  return <>{children}</>;
};

type NamespaceProps = {
  i18nNamespace: string;
  label: string;
  children?: ReactNode;
};

export const TitledNamespaceSettingComponent: FC<NamespaceProps> = ({ children, i18nNamespace, label }) => {
  const [t] = useTranslation(i18nNamespace);
  const namespace = useNamespaceFromParams();
  const [commonTranslation] = useTranslation("commons");
  useDocumentTitle(t(label), commonTranslation("documentTitle.namespaceConfiguration"), namespace.namespace);
  return <>{children}</>;
};

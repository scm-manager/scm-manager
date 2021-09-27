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

import React from "react";
import {
  File,
  Branch,
  IndexResources,
  Links,
  NamespaceStrategies,
  Repository,
  RepositoryCreation,
  RepositoryTypeCollection,
} from "@scm-manager/ui-types";
import { ExtensionPointDefinition } from "./binder";

type RepositoryCreatorSubFormProps = {
  repository: RepositoryCreation;
  onChange: (repository: RepositoryCreation) => void;
  setValid: (valid: boolean) => void;
  disabled?: boolean;
};

export type RepositoryCreatorComponentProps = {
  namespaceStrategies: NamespaceStrategies;
  repositoryTypes: RepositoryTypeCollection;
  index: IndexResources;

  nameForm: React.ComponentType<RepositoryCreatorSubFormProps>;
  informationForm: React.ComponentType<RepositoryCreatorSubFormProps>;
};

export type RepositoryCreatorExtension = {
  subtitle: string;
  path: string;
  icon: string;
  label: string;
  component: React.ComponentType<RepositoryCreatorComponentProps>;
};

export type RepositoryCreator = ExtensionPointDefinition<"repos.creator", RepositoryCreatorExtension>;

export type RepositoryFlags = ExtensionPointDefinition<"repository.flags", { repository: Repository }>;

export type ReposSourcesActionbarExtensionProps = {
  baseUrl: string;
  revision: string;
  branch: Branch | undefined;
  path: string;
  sources: File;
  repository: Repository;
};
export type ReposSourcesActionbarExtension = React.ComponentType<ReposSourcesActionbarExtensionProps>;
export type ReposSourcesActionbar = ExtensionPointDefinition<"repos.sources.actionbar", ReposSourcesActionbarExtension>;

export type ReposSourcesEmptyActionbarExtensionProps = {
  sources: File;
  repository: Repository;
};
export type ReposSourcesEmptyActionbarExtension = ReposSourcesActionbarExtension;
export type ReposSourcesEmptyActionbar = ExtensionPointDefinition<
  "repos.sources.empty.actionbar",
  ReposSourcesEmptyActionbarExtension
>;

export type ReposSourcesTreeWrapperProps = {
  directory: File;
  baseUrl: string;
  revision: string;
};

export type ReposSourcesTreeWrapperExtension = ExtensionPointDefinition<"repos.source.tree.wrapper", ReposSourcesTreeWrapperProps>;

export type ReposSourcesTreeRowRightProps = {
  file: File;
};

export type ReposSourcesTreeRowRightExtension = ExtensionPointDefinition<"repos.sources.tree.row.right", ReposSourcesTreeWrapperProps>;

export type PrimaryNavigationLoginButtonProps = {
  links: Links;
  label: string;
  loginUrl: string;
  from: string;
  to: string;
  className: string;
  content: React.ReactNode;
};

export type PrimaryNavigationLoginButtonExtension = ExtensionPointDefinition<
  "primary-navigation.login",
  PrimaryNavigationLoginButtonProps
>;

export type PrimaryNavigationLogoutButtonProps = {
  links: Links;
  label: string;
  className: string;
  content: React.ReactNode;
};

export type PrimaryNavigationLogoutButtonExtension = ExtensionPointDefinition<
  "primary-navigation.logout",
  PrimaryNavigationLogoutButtonProps
>;

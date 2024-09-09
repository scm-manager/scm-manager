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

import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import ProtocolInformation from "./ProtocolInformation";
import HgAvatar from "./HgAvatar";
import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import HgGlobalConfiguration from "./HgGlobalConfiguration";
import HgBranchInformation from "./HgBranchInformation";
import HgTagInformation from "./HgTagInformation";
import HgRepositoryConfigurationForm from "./HgRepositoryConfigurationForm";

const hgPredicate = (props: extensionPoints.RepositoryDetailsInformation["props"]) => {
  return props.repository && props.repository.type === "hg";
};

binder.bind<extensionPoints.RepositoryDetailsInformation>("repos.repository-details.information", ProtocolInformation, {
  predicate: hgPredicate,
  priority: 100,
});
binder.bind("repos.branch-details.information", HgBranchInformation, { priority: 100, predicate: hgPredicate });

binder.bind<extensionPoints.RepositoryTagDetailsInformation>(
  "repos.tag-details.information",
  HgTagInformation,
  hgPredicate
);
binder.bind("repos.repository-avatar", HgAvatar, hgPredicate);

// bind repository specific configuration

binder.bind<extensionPoints.RepoConfigRoute>("repo-config.route", HgRepositoryConfigurationForm, hgPredicate);

// bind global configuration

cfgBinder.bindGlobal("/hg", "scm-hg-plugin.config.link", "hgConfig", HgGlobalConfiguration, "mercurial");

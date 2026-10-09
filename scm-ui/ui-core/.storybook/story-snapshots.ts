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

import { chromium } from "playwright";
import { defineStorySnapshotTests } from "../../story-snapshots/defineStorySnapshotTests";

await defineStorySnapshotTests({
  url: "http://127.0.0.1:6006",
  launchBrowser: () => chromium.launch(),
  pendingSelector: '[aria-busy="true"]',
  stripRadixCollapsibleSize: true,
});

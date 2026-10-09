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

import type { Browser } from "playwright";
import { afterAll, beforeAll, expect, test } from "vitest";
import { formatHtmlForSnapshot } from "./formatHtmlForSnapshot";

type StoryEntry = {
  id: string;
  title: string;
  exportName: string;
  type: "story";
  tags: string[];
};

type StoryIndex = { entries: Record<string, StoryEntry | { type: string }> };

type Options = {
  url: string;
  launchBrowser: () => Promise<Browser>;
  pendingSelector: string;
  stripRadixCollapsibleSize?: boolean;
};

export async function defineStorySnapshotTests({
  url,
  launchBrowser,
  pendingSelector,
  stripRadixCollapsibleSize = false,
}: Options) {
  const response = await fetch(`${url}/index.json`);
  if (!response.ok) throw new Error(`Could not load Storybook index: ${response.status} ${response.statusText}`);

  const index = (await response.json()) as StoryIndex;
  const stories = Object.values(index.entries).filter(
    (entry): entry is StoryEntry => entry.type === "story" && "tags" in entry && entry.tags.includes("test"),
  );

  if (stories.length === 0) throw new Error("Storybook index contains no testable stories");

  let browser: Browser;
  beforeAll(async () => {
    browser = await launchBrowser();
  });
  afterAll(async () => {
    await browser?.close();
  });

  for (const story of stories) {
    test(`${story.title} ${story.exportName}`, async () => {
      const page = await browser.newPage({
        locale: "en-US",
        timezoneId: "Europe/Berlin",
      });
      try {
        await page.goto(`${url}/iframe.html?id=${encodeURIComponent(story.id)}&viewMode=story`, {
          waitUntil: "domcontentloaded",
          timeout: 15000,
        });
        const root = page.locator("#storybook-root");
        await root.waitFor({ state: "attached", timeout: 10000 });
        await root.getByText("loading translations...", { exact: true }).waitFor({ state: "detached", timeout: 10000 });
        await page.waitForFunction(
          (selector) => {
            const root = document.querySelector("#storybook-root");
            return root && root.innerHTML.trim() && !root.querySelector(selector);
          },
          pendingSelector,
          { timeout: 10000 },
        );

        const html = await root.evaluate((element, stripRadixSize) => {
          const snapshotRoot = element.cloneNode(true) as HTMLElement;
          if (stripRadixSize) {
            snapshotRoot.querySelectorAll<HTMLElement>("[style]").forEach((styledElement) => {
              styledElement.style.removeProperty("--radix-collapsible-content-height");
              styledElement.style.removeProperty("--radix-collapsible-content-width");
            });
          }
          return snapshotRoot.innerHTML;
        }, stripRadixCollapsibleSize);
        expect(html.trim()).not.toBe("");
        expect(await formatHtmlForSnapshot(html)).toMatchSnapshot();
      } finally {
        await page.close();
      }
    }, 30000);
  }
}

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

import React from "react";
import { render, screen } from "@testing-library/react";
import Paginator from "./Paginator";

describe("paginator rendering tests", () => {
  const dummyLink = {
    href: "https://dummy",
  };

  it("should render all buttons but disabled, without links", async () => {
    const collection = {
      page: 10,
      pageTotal: 20,
      _links: {},
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 7);
    buttons.forEach((button) => {
      assert.strictEqual(button.disabled, true, "all buttons should be disabled");
    });
  });

  it("should render buttons for first page", async () => {
    const collection = {
      page: 0,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        next: dummyLink,
        last: dummyLink,
      },
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 5);

    assert.strictEqual(buttons[0].disabled, true, "previous button should be disabled");
    assert.strictEqual(buttons[1].disabled, false, "next button should be enabled");

    const firstButton = buttons[2];
    assert.strictEqual(firstButton.disabled, true, "first button should be disabled");
    assert.strictEqual(firstButton.textContent?.trim(), "1", "first button be 1");

    const nextButton = buttons[3];
    assert.strictEqual(nextButton.disabled, false, "next button should be enabled");
    assert.strictEqual(nextButton.textContent?.trim(), "2", "next button be 2");

    const lastButton = buttons[4];
    assert.strictEqual(lastButton.disabled, false, "last button should be enabled");
    assert.strictEqual(lastButton.textContent?.trim(), "148", "last button be 148");
  });

  it("should render buttons for second page", async () => {
    const collection = {
      page: 1,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: dummyLink,
        next: dummyLink,
        last: dummyLink,
      },
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 6);

    assert.strictEqual(buttons[0].disabled, false, "previous button should be enabled");
    assert.strictEqual(buttons[1].disabled, false, "next button should be enabled");

    const firstButton = buttons[2];
    assert.strictEqual(firstButton.disabled, false, "first button should be enabled");
    assert.strictEqual(firstButton.textContent?.trim(), "1", "first button be 1");

    const currentButton = buttons[3];
    assert.strictEqual(currentButton.disabled, true, "current button should be disabled");
    assert.strictEqual(currentButton.textContent?.trim(), "2", "current button be 2");

    const nextButton = buttons[4];
    assert.strictEqual(nextButton.disabled, false, "next button should be enabled");
    assert.strictEqual(nextButton.textContent?.trim(), "3", "next button be 3");

    const lastButton = buttons[5];
    assert.strictEqual(lastButton.disabled, false, "last button should be enabled");
    assert.strictEqual(lastButton.textContent?.trim(), "148", "last button be 148");
  });

  it("should render buttons for last page", async () => {
    const collection = {
      page: 147,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: dummyLink,
      },
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 5);

    assert.strictEqual(buttons[0].disabled, false, "previous button should be enabled");
    assert.strictEqual(buttons[1].disabled, true, "next button should be disabled");

    const firstButton = buttons[2];
    assert.strictEqual(firstButton.disabled, false, "first button should be enabled");
    assert.strictEqual(firstButton.textContent?.trim(), "1", "first button be 1");

    const nextButton = buttons[3];
    assert.strictEqual(nextButton.disabled, false, "next button should be enabled");
    assert.strictEqual(nextButton.textContent?.trim(), "147", "next button be 147");

    const lastButton = buttons[4];
    assert.strictEqual(lastButton.disabled, true, "last button should be disabled");
    assert.strictEqual(lastButton.textContent?.trim(), "148", "last button be 148");
  });

  it("should render buttons for penultimate page", async () => {
    const collection = {
      page: 146,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: dummyLink,
        next: dummyLink,
        last: dummyLink,
      },
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 6);

    assert.strictEqual(buttons[0].disabled, false, "previous button should be enabled");
    assert.strictEqual(buttons[1].disabled, false, "next button should be enabled");

    const firstButton = buttons[2];
    assert.strictEqual(firstButton.disabled, false, "first button should be enabled");
    assert.strictEqual(firstButton.textContent?.trim(), "1", "first button be 1");

    const previousButton = buttons[3];
    assert.strictEqual(previousButton.disabled, false, "previous button should be enabled");
    assert.strictEqual(previousButton.textContent?.trim(), "146", "previous button be 146");

    const currentButton = buttons[4];
    assert.strictEqual(currentButton.disabled, true, "current button should be disabled");
    assert.strictEqual(currentButton.textContent?.trim(), "147", "current button be 147");

    const lastButton = buttons[5];
    assert.strictEqual(lastButton.disabled, false, "last button should be enabled");
    assert.strictEqual(lastButton.textContent?.trim(), "148", "last button be 148");
  });

  it("should render buttons for a page in the middle", async () => {
    const collection = {
      page: 41,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: dummyLink,
        next: dummyLink,
        last: dummyLink,
      },
      _embedded: {},
    };

    render(<Paginator collection={collection} />);

    const buttons: HTMLButtonElement[] = await screen.findAllByRole("button");

    assert.lengthOf(buttons, 7);

    assert.strictEqual(buttons[0].disabled, false, "previous button should be enabled");
    assert.strictEqual(buttons[1].disabled, false, "next button should be enabled");

    const firstButton = buttons[2] as HTMLButtonElement;
    assert.strictEqual(firstButton.disabled, false, "first button should be enabled");
    assert.strictEqual(firstButton.textContent?.trim(), "1", "first button be 1");

    const previousButton = buttons[3];
    assert.strictEqual(previousButton.disabled, false, "previous button should be enabled");
    assert.strictEqual(previousButton.textContent?.trim(), "41", "previous button be 41");

    const currentButton = buttons[4];
    assert.strictEqual(currentButton.disabled, true, "current button should be disabled");
    assert.strictEqual(currentButton.textContent?.trim(), "42", "current button be 42");

    const nextButton = buttons[5];
    assert.strictEqual(nextButton.disabled, false, "next button should be enabled");
    assert.strictEqual(nextButton.textContent?.trim(), "43", "next button be 43");

    const lastButton = buttons[6];
    assert.strictEqual(lastButton.disabled, false, "last button should be enabled");
    assert.strictEqual(lastButton.textContent?.trim(), "148", "last button be 148");
  });

  it("should call the function with the last previous url", async () => {
    const collection = {
      page: 41,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: {
          href: "https://www.scm-manager.org",
        },
        next: dummyLink,
        last: dummyLink,
      },
      _embedded: {},
    };

    let urlToOpen;
    const callMe = (url: string) => {
      urlToOpen = url;
    };

    render(<Paginator collection={collection} onPageChange={callMe} />);

    const previousButton = screen.getByRole("button", { name: /paginator.previous/i });
    previousButton.click();

    assert.strictEqual(urlToOpen, "https://www.scm-manager.org", "the url passed to the function is not correct");
  });
});

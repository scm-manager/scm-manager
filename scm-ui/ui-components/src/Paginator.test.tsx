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
import { mount, shallow } from "@scm-manager/ui-tests";
import React from "react";
import Paginator from "./Paginator";

xdescribe("paginator rendering tests", () => {
  const dummyLink = {
    href: "https://dummy",
  };

  it("should render all buttons but disabled, without links", () => {
    const collection = {
      page: 10,
      pageTotal: 20,
      _links: {},
      _embedded: {},
    };

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(7);
    buttons.forEach((button) => {
      // @ts-ignore ???
      expect(button.props.disabled).toBeTruthy();
    });
  });

  it("should render buttons for first page", () => {
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

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(5);

    // previous button
    expect(buttons.get(0).props.disabled).toBeTruthy();
    // last button
    expect(buttons.get(1).props.disabled).toBeFalsy();
    // first button
    const firstButton = buttons.get(2).props;
    expect(firstButton.disabled).toBeTruthy();
    expect(firstButton.label).toBe(1);

    // next button
    const nextButton = buttons.get(3).props;
    expect(nextButton.disabled).toBeFalsy();
    expect(nextButton.label).toBe("2");

    // last button
    const lastButton = buttons.get(4).props;
    expect(lastButton.disabled).toBeFalsy();
    expect(lastButton.label).toBe("148");
  });

  it("should render buttons for second page", () => {
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

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(6);

    // previous button
    expect(buttons.get(0).props.disabled).toBeFalsy();
    // last button
    expect(buttons.get(1).props.disabled).toBeFalsy();
    // first button
    const firstButton = buttons.get(2).props;
    expect(firstButton.disabled).toBeFalsy();
    expect(firstButton.label).toBe("1");

    // current button
    const currentButton = buttons.get(3).props;
    expect(currentButton.disabled).toBeTruthy();
    expect(currentButton.label).toBe(2);

    // next button
    const nextButton = buttons.get(4).props;
    expect(nextButton.disabled).toBeFalsy();
    expect(nextButton.label).toBe("3");

    // last button
    const lastButton = buttons.get(5).props;
    expect(lastButton.disabled).toBeFalsy();
    expect(lastButton.label).toBe("148");
  });

  it("should render buttons for last page", () => {
    const collection = {
      page: 147,
      pageTotal: 148,
      _links: {
        first: dummyLink,
        prev: dummyLink,
      },
      _embedded: {},
    };

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(5);

    // previous button
    expect(buttons.get(0).props.disabled).toBeFalsy();
    // last button
    expect(buttons.get(1).props.disabled).toBeTruthy();
    // first button
    const firstButton = buttons.get(2).props;
    expect(firstButton.disabled).toBeFalsy();
    expect(firstButton.label).toBe("1");

    // next button
    const nextButton = buttons.get(3).props;
    expect(nextButton.disabled).toBeFalsy();
    expect(nextButton.label).toBe("147");

    // last button
    const lastButton = buttons.get(4).props;
    expect(lastButton.disabled).toBeTruthy();
    expect(lastButton.label).toBe(148);
  });

  it("should render buttons for penultimate page", () => {
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

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(6);

    // previous button
    expect(buttons.get(0).props.disabled).toBeFalsy();
    // last button
    expect(buttons.get(1).props.disabled).toBeFalsy();

    // first button
    const firstButton = buttons.get(2).props;
    expect(firstButton.disabled).toBeFalsy();
    expect(firstButton.label).toBe("1");

    const currentButton = buttons.get(3).props;
    expect(currentButton.disabled).toBeFalsy();
    expect(currentButton.label).toBe("146");

    // current button
    const nextButton = buttons.get(4).props;
    expect(nextButton.disabled).toBeTruthy();
    expect(nextButton.label).toBe(147);

    // last button
    const lastButton = buttons.get(5).props;
    expect(lastButton.disabled).toBeFalsy();
    expect(lastButton.label).toBe("148");
  });

  it("should render buttons for a page in the middle", () => {
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

    const paginator = shallow(<Paginator collection={collection} />);
    const buttons = paginator.find("Button");
    expect(buttons.length).toBe(7);

    // previous button
    expect(buttons.get(0).props.disabled).toBeFalsy();
    // next button
    expect(buttons.get(1).props.disabled).toBeFalsy();

    // first button
    const firstButton = buttons.get(2).props;
    expect(firstButton.disabled).toBeFalsy();
    expect(firstButton.label).toBe("1");

    // previous Button
    const previousButton = buttons.get(3).props;
    expect(previousButton.disabled).toBeFalsy();
    expect(previousButton.label).toBe("41");

    // current button
    const currentButton = buttons.get(4).props;
    expect(currentButton.disabled).toBeTruthy();
    expect(currentButton.label).toBe(42);

    // next button
    const nextButton = buttons.get(5).props;
    expect(nextButton.disabled).toBeFalsy();
    expect(nextButton.label).toBe("43");

    // last button
    const lastButton = buttons.get(6).props;
    expect(lastButton.disabled).toBeFalsy();
    expect(lastButton.label).toBe("148");
  });

  it("should call the function with the last previous url", () => {
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

    const paginator = mount(<Paginator collection={collection} onPageChange={callMe} />);
    paginator.find("Button.pagination-previous").simulate("click");

    expect(urlToOpen).toBe("https://www.scm-manager.org");
  });
});

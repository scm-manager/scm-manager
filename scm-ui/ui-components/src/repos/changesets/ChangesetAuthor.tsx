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
import React, { FC } from "react";
import { Changeset } from "@scm-manager/ui-types";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import { binder } from "@scm-manager/ui-extensions";

type Props = WithTranslation & {
  changeset: Changeset;
};

type PersonType = {
  name: string;
  mail?: string;
};

type PersonProps = {
  person: PersonType;
};

const Person: FC<PersonProps> = ({ person }) => {
  const [t] = useTranslation("repos");
  if (person.mail) {
    return (
      <a href={"mailto:" + person.mail} title={t("changeset.author.mailto") + " " + person.mail}>
        {person.name}
      </a>
    );
  }
  return <>{person.name}</>;
};

type PersonsProps = {
  persons: PersonType[];
  label: string;
};

const Persons: FC<PersonsProps> = ({ persons, label }) => {
  const [t] = useTranslation("repos");
  if (persons.length === 1) {
    return (
      <>
        {t(label)} <Person person={persons[0]} />
      </>
    );
  }
  return (
    <>
      {t(label)}{" "}
      <a title={label + ":\n" + persons.map(person => "- " + person.name).join("\n")}>
        {t("changesets.authors.more", { count: persons.length })}
      </a>
    </>
  );
};

class ChangesetAuthor extends React.Component<Props> {
  render() {
    const { changeset, t } = this.props;

    const authorLine = [];

    if (changeset.author) {
      authorLine.push(<Persons persons={[changeset.author]} label={"changesets.authors.authoredBy"} />);
    }

    const commiters = this.getCommiters();
    if (commiters.length > 0) {
      authorLine.push(<Persons persons={commiters} label={"changesets.authors.committedBy"} />);
    }

    const coAuthors = this.getCoAuthors();
    if (coAuthors.length > 0) {
      authorLine.push(<Persons persons={coAuthors} label={"changesets.authors.coAuthoredBy"} />);
    }

    // extensions
    const extensions = binder.getExtensions("changesets.author.suffix", this.props);
    if (extensions) {
      coAuthors.push(...extensions);
    }

    return (
      <>
        {authorLine.map((p, i) => {
          if (i === 0) {
            return <>{p}</>;
          } else if (i + 1 === authorLine.length) {
            return (
              <>
                {" "}
                {t("changesets.authors.and")} {p}{" "}
              </>
            );
          } else {
            return <>, {p}</>;
          }
        })}
      </>
    );
  }

  getCoAuthors() {
    return this.filterTrailersByType("Co-authored-by");
  }

  getCommiters() {
    return this.filterTrailersByType("Committed-by");
  }

  filterTrailersByType(t: string) {
    return this.props.changeset.trailers.filter(p => p.trailerType === t).map(trailer => trailer.person);
  }
}

export default withTranslation("repos")(ChangesetAuthor);

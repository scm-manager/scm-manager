import React from "react"
import type { Changeset } from "@scm-manager/ui-types"
import {ExtensionPoint} from "@scm-manager/ui-extensions";
import {Link} from "react-router-dom";
import {History} from "history";
import { NavLink } from "@scm-manager/ui-components";
import {withRouter} from "react-router-dom";


type Props = {
  changeset: Changeset,
  location: any
}

class ChangesetRow extends React.Component<Props> {


  render() {
    const { changeset } = this.props;
    // todo: i18n
    return (
        <tr>
          <td>
            <ExtensionPoint
              name="repos.changeset-table.information"
              renderAll={true}
              props={{ changeset }}
            />
            <p> <NavLink to={`${this.props.location.pathname}/commit/${changeset.id}`} label={changeset.description}/></p>
            <p className="is-size-7">Changeset { changeset.id } commited at { changeset.date }</p>
            <p className="is-size-7">{changeset.author.name} <a href={"mailto:" + changeset.author.mail}>&lt;{changeset.author.mail}&gt;</a></p></td>
        </tr>
    );

  }
}

export default withRouter(ChangesetRow);

import React from "react"
import type { Changeset } from "@scm-manager/ui-types"
import {ExtensionPoint} from "@scm-manager/ui-extensions";

type Props = {
  changeset: Changeset
}

class ChangesetRow extends React.Component<Props> {

  render() {
    const { changeset } = this.props;
    // todo: i18n
    return <tr>
      <td>
        <ExtensionPoint
          name="repos.changeset-table.information"
          renderAll={true}
          props={{ changeset }}
        />
        <p>{changeset.description}</p>
        <p className="is-size-7">Changeset { changeset.id } commited at { changeset.date }</p>
        <p className="is-size-7">{changeset.author.name} <a href={"mailto:" + changeset.author.mail}>&lt;{changeset.author.mail}&gt;</a></p></td>
    </tr>
  }
}

export default ChangesetRow;

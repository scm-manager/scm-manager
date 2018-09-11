import React from "react"
import type { Changeset } from "@scm-manager/ui-types"

type Props = {
  changeset: Changeset
}

class ChangesetRow extends React.Component<Props> {

  // Todo: Add extension point to author field
  render() {
    const {changeset} = this.props;
    return <tr>
      <td>{ changeset.author.name }</td>
      <td>{ changeset.description }</td>
      <td>{ changeset.date }</td>
    </tr>
  }
}

export default ChangesetRow;

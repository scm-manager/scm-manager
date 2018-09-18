// @flow
import ChangesetRow from "./ChangesetRow";
import React from "react";

type Props = {
  changesets: Changeset[]
}

class ChangesetTable extends React.Component<Props> {

  render() {
    const {changesets} = this.props;
    return <div>
      <table className="table is-hoverable is-fullwidth is-striped is-bordered">
        <thead>
        <tr>
          <th>Changesets</th>
        </tr>
        </thead>
        <tbody>
        {changesets.map((changeset, index) => {
          return <ChangesetRow key={index} changeset={changeset}/>;
        })}
        </tbody>
      </table>
    </div>
  }
}

export default ChangesetTable;

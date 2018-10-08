//@flow
import React from "react";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import type { Changeset } from "@scm-manager/ui-types";

type Props = {
  changeset: Changeset
};

class ChangesetAvatar extends React.Component<Props> {
  render() {
    const { changeset } = this.props;
    return (
      <ExtensionPoint
        name="repos.changeset-table.information"
        renderAll={true}
        props={{ changeset }}
      >
        {/* extension should render something like this:  */}
        {/* <div className="image is-64x64">              */}
        {/*   <figure className="media-left">             */}
        {/*    <Image src="/some/image.jpg" alt="Logo" /> */}
        {/*   </figure>                                   */}
        {/* </div>                                        */}
      </ExtensionPoint>
    );
  }
}

export default ChangesetAvatar;

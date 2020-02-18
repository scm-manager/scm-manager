import React from "react";
import { Me, Links } from "@scm-manager/ui-types";
import { Link } from "react-router-dom";
import { binder } from "@scm-manager/ui-extensions";
import { AvatarWrapper, AvatarImage } from "../avatar";

type Props = {
  me?: Me;
  version: string;
  links: Links;
};

class Footer extends React.Component<Props> {
  render() {
    const { me, version, links } = this.props;
    if (!me) {
      return "";
    }

    const extensions = binder.getExtensions("footer.links", { me, links });

    return (
      <footer className="footer">
        <div className="container is-centered has-text-centered ">
          <p className="is-size-6 columns is-centered">
            <AvatarWrapper>
              <figure className="media-left">
                <p className="image is-24x24 is-rounded">
                  <AvatarImage person={me} representation="rounded" />
                </p>
              </figure>
            </AvatarWrapper>
            <Link to={"/me"}>{me.displayName}</Link>
          </p>
          <hr className={"has-background-grey-lighter"} />
          <p>
            <a href="https://www.scm-manager.org/" target="_blank">
              SCM-Manager {version}
            </a>
            {extensions.map(Ext => (
              <>
                {" "}
                | <Ext />
              </>
            ))}
          </p>
          <br />
          <p>
            Powered by{" "}
            <a target="_blank" href="https://cloudogu.com/">
              Cloudogu GmbH
            </a>{" "}
            | Learn more about{" "}
            <a href="https://www.scm-manager.org/" target="_blank">
              SCM-Manager
            </a>
          </p>
        </div>
      </footer>
    );
  }
}

export default Footer;

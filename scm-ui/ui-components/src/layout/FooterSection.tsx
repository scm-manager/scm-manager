import React, { FC, ReactNode } from "react";
import styled from "styled-components";

type Props = {
  title: ReactNode;
};

const Title = styled.div`
  font-weight: bold;
  margin-bottom: 0.5rem;
`;

const Menu = styled.ul`
  padding-left: 1.1rem;
`;

const FooterSection: FC<Props> = ({ title, children }) => {
  return (
    <section className="column is-one-third">
      <Title>{title}</Title>
      <Menu>{children}</Menu>
    </section>
  );
};

export default FooterSection;

package sonia.scm.repository;

public interface GitHeadModifier {

  void modify(Repository repository, String head);

}

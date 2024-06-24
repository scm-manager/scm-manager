import sonia.scm.repository.api.IgnoreWhitespaceLevel;
  public static final String DIFF_IGNORE_WHITESPACE = "diff --git a/a.txt b/a.txt\n" +
    "index 2f8bc28..fc3f0ba 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n";

  public static final String DIFF_WITH_WHITESPACE = "diff --git a/a.txt b/a.txt\n" +
    "index 2f8bc28..fc3f0ba 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1,2 +1,2 @@\n" +
    " a\n" +
    "-line for blame\n" +
    "+line                          for blame\n";

  @Test
  public void shouldIgnoreWhiteSpace() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.ALL);
    diffCommandRequest.setRevision("whitespace");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_IGNORE_WHITESPACE, output.toString());
  }

  @Test
  public void shouldNotIgnoreWhiteSpace() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.NONE);
    diffCommandRequest.setRevision("whitespace");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_WITH_WHITESPACE, output.toString());
  }


  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-whitespace-test.zip";
  }
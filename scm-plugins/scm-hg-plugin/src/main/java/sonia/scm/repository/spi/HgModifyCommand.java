package sonia.scm.repository.spi;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.PushCommand;
import com.aragost.javahg.commands.RemoveCommand;
import org.apache.commons.lang.StringUtils;
import sonia.scm.ContextEntry;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sonia.scm.AlreadyExistsException.alreadyExists;
import static sonia.scm.ContextEntry.ContextBuilder.entity;

public class HgModifyCommand implements ModifyCommand {

  private final HgRepositoryHandler handler;
  private HgCommandContext context;
  private final HgWorkdirFactory workdirFactory;

  public HgModifyCommand(HgRepositoryHandler handler, HgCommandContext context, HgWorkdirFactory workdirFactory) {
    this.handler = handler;
    this.context = context;
    this.workdirFactory = workdirFactory;
  }

  @Override
  public String execute(ModifyCommandRequest request) {

    try (WorkingCopy<com.aragost.javahg.Repository> workingCopy = workdirFactory.createWorkingCopy(context)) {
      Repository workingRepository = workingCopy.getWorkingRepository();
      request.getRequests().forEach(
        partialRequest -> {
          try {
            partialRequest.execute(new Worker() {
              @Override
              public void delete(String toBeDeleted) {
                RemoveCommand.on(workingRepository).execute(toBeDeleted);
              }

              @Override
              public void create(String toBeCreated, File file, boolean overwrite) throws IOException {
                Path targetFile = new File(workingRepository.getDirectory(), toBeCreated).toPath();
                if (overwrite) {
                  Files.move(file.toPath(), targetFile, REPLACE_EXISTING);
                } else {
                  try {
                    Files.move(file.toPath(), targetFile);
                  } catch (FileAlreadyExistsException e) {
                    throw alreadyExists(createFileContext(toBeCreated));
                  }
                }
                try {
                  addFileToHg(targetFile.toFile());
                } catch (Exception e) {
                  throwInternalRepositoryException("could not add new file to index", e);
                }
              }

              @Override
              public void modify(String path, File file) {

              }

              @Override
              public void move(String sourcePath, String targetPath) {

              }

              private void createDirectories(Path targetFile) throws IOException {
                try {
                  Files.createDirectories(targetFile.getParent());
                } catch (FileAlreadyExistsException e) {
                  throw alreadyExists(createFileContext(targetFile.toString()));
                }
              }

              private ContextEntry.ContextBuilder createFileContext(String path) {
                ContextEntry.ContextBuilder contextBuilder = entity("file", path);
                if (!StringUtils.isEmpty(request.getBranch())) {
                  contextBuilder.in("branch", request.getBranch());
                }
                contextBuilder.in(context.getScmRepository());
                return contextBuilder;
              }

              private void addFileToHg(File file) {
                workingRepository.workingCopy().add(file.getAbsolutePath());
              }

            });
          } catch (IOException e) {
            e.printStackTrace(); // TODO
          }
        }
      );

      CommitCommand.on(workingRepository).user(String.format("%s <%s>", request.getAuthor().getName(), request.getAuthor().getMail())).message(request.getCommitMessage()).execute();
      List<Changeset> execute = PushCommand.on(workingRepository).execute();
      System.out.println(execute);
      return execute.get(0).getNode();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private String throwInternalRepositoryException(String message, Exception e) {
    throw new InternalRepositoryException(context.getScmRepository(), message, e);
  }
}

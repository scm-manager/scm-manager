package sonia.scm.repository.spi;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.PushCommand;
import com.aragost.javahg.commands.RemoveCommand;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.repository.util.WorkingCopy;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
              public void create(String toBeCreated, File file, boolean overwrite) {

              }

              @Override
              public void modify(String path, File file) {

              }

              @Override
              public void move(String sourcePath, String targetPath) {

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
}

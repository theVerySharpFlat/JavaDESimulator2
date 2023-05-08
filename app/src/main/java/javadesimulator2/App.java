/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package javadesimulator2;

import com.google.common.io.Files;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.app.Application;
import imgui.app.Configuration;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiConfigFlags;
import java.util.Map;
import javadesimulator2.GUI.*;

public class App extends Application {
  @Override
  protected void configure(Configuration config) {
    config.setTitle("Dear ImGui is Awesome!");
  }

  @Override
  protected void initImGui(final Configuration config) {
    super.initImGui(config);

    final ImGuiIO io = ImGui.getIO();
    // io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard
    // Controls
    io.addConfigFlags(ImGuiConfigFlags.DockingEnable); // Enable Docking
    io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);

    nodeEditor = new NodeEditor();
  }

  public void openFileDialog(String id, String title) {
    ImGuiFileDialog.openDialog(id, title, ".jde2", ".", "", 1, 0, ImGuiFileDialogFlags.None);
  }

  public void save() {
    if (nodeEditor.getLastSavePath() == null) {
      openFileDialog("browse-save", "Save As");
    } else {
      nodeEditor.serialize(nodeEditor.getLastSavePath());
    }
  }

  @Override
  public void process() {
    ImGui.dockSpaceOverViewport();

    ImGui.beginMainMenuBar();
    if (ImGui.beginMenu("File")) {

      if (ImGui.menuItem("save as")) {
        openFileDialog("browse-save", "Save As");
      }

      if (ImGui.menuItem("save")) {
        save();
      }

      if (ImGui.menuItem("open")) {
        openFileDialog("browse-open", "Open");
      }

      if (ImGui.menuItem("new project")) {
        save();
        nodeEditor.newSchematic(Schematic.Type.ROOT);
      }

      if (ImGui.menuItem("new component")) {
        save();
        nodeEditor.newSchematic(Schematic.Type.COMPONENT);
      }

      if(ImGui.menuItem("optimize IDs")) {
        nodeEditor.optimizeIDs();
      }

      ImGui.endMenu();
    }

    ImGui.endMainMenuBar();

    if (ImGuiFileDialog.display("browse-save", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
      if (ImGuiFileDialog.isOk()) {
        Map<String, String> filenames = ImGuiFileDialog.getSelection();
        if (filenames != null && filenames.size() > 0) {
          nodeEditor.serialize(filenames.values().stream().findFirst().get());
        } else if (ImGuiFileDialog.getFilePathName() != null
            && ImGuiFileDialog.getFilePathName().length() > 0) {
          String path = ImGuiFileDialog.getFilePathName();
          String extension = Files.getFileExtension(path);
          if (!extension.equals("jde2")) {
            path = path + ".jde2";
          }
          nodeEditor.serialize(path);
        }
      }
      ImGuiFileDialog.close();
    }

    if (ImGuiFileDialog.display("browse-open", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
      if (ImGuiFileDialog.isOk()) {
        Map<String, String> filenames = ImGuiFileDialog.getSelection();
        if (filenames != null && filenames.size() > 0) {
          nodeEditor.load(filenames.values().stream().findFirst().get());
        }
      }
      ImGuiFileDialog.close();
    }

    nodeEditor.showSidebar(true);

    nodeEditor.show(true);
  }

  public static void main(String[] args) {
    launch(new App());
  }

  private NodeEditor nodeEditor = null;
}

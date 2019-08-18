import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class FileManager {

    private static Path mainFolder = Paths.get(System.getProperty("user.home"),"AppData\\Roaming\\FSBackup");
    private static Pattern saveFilePattern = Pattern.compile("([^\\s]+(\\.(?i)(sl2|sl2\\.bak))$)");
    private static Matcher matcher;

    static void setup() {

        findOrCreateFolder(mainFolder);

        for (CompatibleGame game : CompatibleGame.values()) {

            Path originalGameFolder = getGamePath(game);

            if (Files.exists(originalGameFolder)) {

                Path newGameFolder = mainFolder.resolve(originalGameFolder.getFileName());
                findOrCreateFolder(newGameFolder);
            }
        }
    }

    private static void findOrCreateFolder(Path folder) {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectory(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Path getGamePath(CompatibleGame game) {
        return mainFolder.resolve("..\\" + game);
    }

    private static Path getGameBackupPath(CompatibleGame game) {
        return mainFolder.resolve(getGamePath(game).getFileName());
    }

    private static void copyAndOverwriteFiles(Path source, Path destination) {

        try (Stream<Path> files = Files.walk(source)) {

            findOrCreateFolder(destination);

            files
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    matcher = saveFilePattern.matcher(file.getFileName().toString());
                    if (matcher.matches()) {
                        try {
                            Files.copy(file.toAbsolutePath(), destination.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void createBackup(CompatibleGame game, String backupName) {

        Path gameToBackup = getGamePath(game);
        Path backup = getGameBackupPath(game).resolve(backupName);

        copyAndOverwriteFiles(gameToBackup, backup);
    }

    static void renameBackup(CompatibleGame game, String backupName, String newBackupName) {

        Path gameToRename = getGameBackupPath(game);
        Path originalBackup = gameToRename.resolve(backupName);
        Path newBackup = gameToRename.resolve(newBackupName);

        if (Files.exists(originalBackup) && Files.isDirectory(originalBackup)) {
            copyAndOverwriteFiles(originalBackup, newBackup);
            deleteBackup(game, originalBackup.toString());
        }
    }

    static void deleteBackup(CompatibleGame game, String backupName) {

        Path gameBackupPath = getGameBackupPath(game);
        Path saveToDelete = gameBackupPath.resolve(backupName);

        try (Stream<Path> files = Files.walk(saveToDelete)) {

            files
                .filter(file -> !file.getFileName().toString().equals(saveToDelete.toString()))
                .forEach(file -> {
                    try {
                        if (!Files.isDirectory(file))
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            });

            Files.deleteIfExists(saveToDelete);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadBackup(CompatibleGame game, String backupName) {

        Path gamePath = getGamePath(game);
        Path saveToLoad = getGameBackupPath(game).resolve(backupName);

        try (Stream<Path> files = Files.walk(gamePath)) {

            Optional<Path> saveDir = files
                .filter(Files::isDirectory)
                .filter(dir -> !Objects.equals(dir.getFileName().toString(), game.toString()))
                .findFirst();

            saveDir.ifPresent(path -> copyAndOverwriteFiles(saveToLoad, saveDir.get()));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static ArrayList<CompatibleGame> getAllDetectedGames() {
        ArrayList<CompatibleGame> allGames = new ArrayList<>();
        for (CompatibleGame game : CompatibleGame.values()) {
            if (Files.exists(getGameBackupPath(game)))
                allGames.add(game);
        }
        return allGames;
    }

    static ArrayList<String> getAllBackups(CompatibleGame game) {

        ArrayList<String> allBackups;

        try (Stream<Path> folders = Files.walk(getGameBackupPath(game))) {

            allBackups = new ArrayList<>();

            folders
                .filter(Files::isDirectory)
                .filter(dir -> !Objects.equals(dir.getFileName().toString(), game.toString()))
                .forEach(dir -> allBackups.add(dir.getFileName().toString()));

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return allBackups;
    }
}

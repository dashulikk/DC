package ua.lab8.server;

import ua.lab8.model.File;
import ua.lab8.model.Folder;

import java.sql.*;
import java.time.LocalDateTime;

public class DAO {
    private final Connection connection;

    public DAO() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/filesys", "root", "Dar2003dar28!");
            System.out.println("Connection to database is successful");
        } catch (SQLException e) {
            System.out.println("ERROR: cannot connect to database");
            System.out.println(e.getMessage());
            throw new RuntimeException();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    public boolean saveIfAbsent(Folder folder) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT folder_name FROM filesys.folder WHERE folder_name = ?");
            ps.setString(1, folder.name);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return false;
            ps = connection.prepareStatement("INSERT INTO `filesys`.`folder` (`folder_name`) VALUES (?)");
            ps.setString(1, folder.name);
            ps.execute();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean saveIfAbsent(File file) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM file WHERE file_name = ? AND folder_name = ?");
            ps.setString(1, file.fileName);
            ps.setString(2, file.folderName);
            if (ps.executeQuery().next())
                return false;
            ps = connection.prepareStatement("INSERT INTO file " +
                    "(file_name, folder_name, size, is_visible, is_readable, is_writeable, last_updated) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, file.fileName);
            ps.setString(2, file.folderName);
            ps.setLong(3, file.size);
            ps.setBoolean(4, file.isVisible);
            ps.setBoolean(5, file.isReadable);
            ps.setBoolean(6, file.isWritable);
            ps.setTimestamp(7, Timestamp.valueOf(file.lastUpdated));
            ps.execute();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String readAllFolders() {
        try {
            StringBuilder sb = new StringBuilder();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM folder");
            while (rs.next())
                sb.append(new Folder(rs.getString("folder_name"))).append('\n');
            sb.append("end");
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String readAllFilesInFolder(Folder folder) {
        try {
            StringBuilder sb = new StringBuilder();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM file WHERE folder_name = ?");
            ps.setString(1, folder.name);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sb.append(new File(rs.getString("folder_name"),
                        rs.getString("file_name"),
                        rs.getLong("size"),
                        rs.getBoolean("is_visible"),
                        rs.getBoolean("is_readable"),
                        rs.getBoolean("is_writeable"),
                        rs.getTimestamp("last_updated").toLocalDateTime()
                        )).append('\n');
            }
            sb.append("end");
            return sb.toString();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFile(String folderName, String fileName) {
        // TODO check if file is present is the specified folder if not return ~~file not found~~
        // TODO if present deletes file and return file has been successfully deleted
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM file WHERE folder_name = ? AND file_name = ?");
            ps.setString(1, folderName);
            ps.setString(2, fileName);
            if (!ps.executeQuery().next())
                return "File \"" + folderName + "/" + fileName + "\" haven't been found";
            ps = connection.prepareStatement("DELETE FROM file WHERE folder_name = ? AND file_name = ?");
            ps.setString(1, folderName);
            ps.setString(2, fileName);
            ps.execute();
            return "File \"" + folderName + "/" + fileName + "\" has been successfully deleted";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFiles(String folderName) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM file WHERE folder_name = ?");
            ps.setString(1, folderName);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFolder(String folderName) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM folder WHERE folder_name = ?");
            ps.setString(1, folderName);
            ps.execute();
            return "Folder \"" + folderName + "\" is successfully deleted";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFile(String folderName, String fileName, String attr, String value) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE file SET " +
                    attr + " = ?, last_updated = ? WHERE folder_name = ? AND file_name = ?");
            switch (attr) {
                case "file_name", "folder_name" -> ps.setString(1, value);
                case "size" -> ps.setLong(1, Long.parseLong(value));
                case "is_visible", "is_readable", "is_writeable" -> ps.setBoolean(1, Boolean.parseBoolean(value));
                default -> throw new IllegalArgumentException("Attribute \"" + attr + "\" is not recognized");
            }
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, folderName);
            ps.setString(4, fileName);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyFile(String srcFolder, String fileName, String dstFolder) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM file WHERE folder_name = ? AND file_name = ?");
            ps.setString(1, srcFolder);
            ps.setString(2, fileName);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                throw new IllegalArgumentException("There is no file \"" + srcFolder + "/" + fileName + "\"");
            saveIfAbsent(new File(dstFolder, fileName, rs.getLong("size"), rs.getBoolean("is_visible"),
                    rs.getBoolean("is_readable"), rs.getBoolean("is_writeable")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

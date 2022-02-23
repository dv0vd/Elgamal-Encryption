package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class Controller {

    private final String FILE_LABEL = "Файл...";
    private final String DIRECTORY_LABEL = "Каталог сохранения...";
    private final String KEYS_FILE_LABEL = "Файл с ключом...";
    private File file, directory, keysFile;

    @FXML
    private Button chooseKeysFileButton;

    @FXML
    private Label chooseFileLabel;

    @FXML
    private Label chooseKeysFileLabel;

    @FXML
    private RadioButton encryptingRadioButton;

    @FXML
    private RadioButton decryptingRadioButton;

    @FXML
    private Label chooseDirectoryLabel;

    @FXML
    void chooseDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите место сохранения результата");
        directory = directoryChooser.showDialog(null);
        if(directory != null) {
            chooseDirectoryLabel.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    public void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для шифрования/дешифрования");
        file =  fileChooser.showOpenDialog(null);
        if(file != null) {
            chooseFileLabel.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void chooseDecrypting(ActionEvent event) {
        encryptingRadioButton.setSelected(false);
        chooseKeysFileLabel.setDisable(false);
        chooseKeysFileButton.setDisable(false);
    }

    @FXML
    void chooseEncrypting(ActionEvent event) {
        decryptingRadioButton.setSelected(false);
        chooseKeysFileLabel.setDisable(true);
        chooseKeysFileButton.setDisable(true);
    }

    @FXML
    void chooseKeysFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с ключом");
        keysFile =  fileChooser.showOpenDialog(null);
        if(keysFile != null) {
            chooseKeysFileLabel.setText(keysFile.getAbsolutePath());
        }
    }

    void dialogError(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setContentText("Выберите все файлы и директории!");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    void finish() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успех");
        alert.setContentText("Операция выполнена успешно!");
        alert.setHeaderText(null);
        alert.showAndWait();
        chooseKeysFileLabel.setText(KEYS_FILE_LABEL);
        chooseDirectoryLabel.setText(DIRECTORY_LABEL);
        chooseFileLabel.setText(FILE_LABEL);
    }

    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }

    @FXML
    void letsgo(ActionEvent event) throws Exception {
        if((chooseFileLabel.getText().equals(FILE_LABEL)) || (chooseDirectoryLabel.getText().equals(DIRECTORY_LABEL))) {
            dialogError();
            return;
        }
        if(decryptingRadioButton.isSelected() && chooseKeysFileLabel.getText().equals(KEYS_FILE_LABEL)) {
            dialogError();
            return;
        }
        byte[] fileByte  = new byte[(int) file.length()];
        try {
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileByte);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(encryptingRadioButton.isSelected()) {
            Crypto crypto = new Crypto();
            crypto.establishKeys("secp256r1");
            byte[] result = crypto.encrypt(fileByte);
            FileOutputStream outputStream = new FileOutputStream(directory+"/зашифрованный файл");
            String extension = getFileExtension(file);
            byte[] extensionBytes = extension.getBytes();
            outputStream.write((byte)extensionBytes.length);
            outputStream.write(extensionBytes);
            outputStream.write(result);
            outputStream.close();
            outputStream = new FileOutputStream(directory+"/файл с ключом");
            byte[] privateKeyBytes = crypto.key.getPrivate().getEncoded();
            outputStream.write(privateKeyBytes);
            outputStream.close();
        } else {
            Crypto crypto = new Crypto();
            FileInputStream inputStream = new FileInputStream(keysFile);
            byte[] privateKey = new byte[inputStream.available()];
            inputStream.read(privateKey);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PrivateKey privateKey2 = keyFactory.generatePrivate(privateKeySpec);
            crypto.privateKey = keyFactory.generatePrivate(privateKeySpec);
            FileInputStream inputStream1 = new FileInputStream(file);
            byte[] size = new byte[1];
            inputStream1.read(size,0,1);
            byte[] extensionBytes = new byte[size[0]];
            inputStream1.read(extensionBytes,0,size[0]);
            String extension = new String(extensionBytes);
            byte[] encryptedFile = new byte[inputStream1.available()];
            inputStream1.read(encryptedFile);
            byte[] result = crypto.decrypt(encryptedFile);
            FileOutputStream out = new FileOutputStream(directory+"/расшифрованный файл."+extension);
            out.write(result);
            out.close();
            inputStream.close();
            inputStream1.close();
        }
        finish();
    }
}

/*
 * Copyright (C) 2019 European Spallation Source ERIC.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.phoebus.logbook.olog.ui.write;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.phoebus.logbook.LogService;
import org.phoebus.logbook.olog.ui.LogbookUIPreferences;
import org.phoebus.logbook.olog.ui.Messages;
import org.phoebus.ui.application.PhoebusApplication;
import org.phoebus.util.time.TimestampFormats;

import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;

public class FieldsViewController implements Initializable {

    // Credentials of user making entry.
    @FXML
    private Label userFieldLabel;
    @FXML
    private Label passwordFieldLabel;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private Label levelLabel;
    @FXML
    private TextField dateField;
    @FXML
    private ComboBox<String> levelSelector;

    // Title and body of log entry
    @FXML
    private Label titleLabel;
    @FXML
    private TextField titleField;
    @FXML
    private VBox logbooks;
    @FXML
    private TextArea textArea;
    @FXML
    private VBox root;

    private SimpleStringProperty textAreaContent = new SimpleStringProperty();

    private LogEntryModel model;

    public FieldsViewController(LogEntryModel logEntryModel) {
        this.model = logEntryModel;
    }

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        levelLabel.setText(LogbookUIPreferences.level_field_name);
        Instant now = Instant.now();
        dateField.setText(TimestampFormats.DATE_FORMAT.format(now));
        model.setDate(now);

        logbooks.getChildren().add(new LogbooksTagsView(model));

        levelSelector.setItems(model.getLevels());

        model.fetchLevels();
        model.addLevelListener((ListChangeListener.Change<? extends String> c) ->
        {
            if (c.next()) {
                if (!model.getLevels().isEmpty() && model.getLevels().get(0) != null)
                    levelSelector.getSelectionModel().select(model.getLevels().get(0));
                else
                    levelSelector.getSelectionModel().select(Messages.Normal);
            }
        });

        userField.textProperty().addListener((changeListener, oldVal, newVal) ->
        {
            if (newVal.trim().isEmpty())
                userFieldLabel.setTextFill(Color.RED);
            else
                userFieldLabel.setTextFill(Color.BLACK);
        });

        passwordField.textProperty().addListener((changeListener, oldVal, newVal) ->
        {
            if (newVal.trim().isEmpty())
                passwordFieldLabel.setTextFill(Color.RED);
            else
                passwordFieldLabel.setTextFill(Color.BLACK);
        });

        titleField.textProperty().addListener((changeListener, oldVal, newVal) ->
        {
            if (newVal.trim().isEmpty())
                titleLabel.setTextFill(Color.RED);
            else
                titleLabel.setTextFill(Color.BLACK);
        });

        model.getUpdateCredentialsProperty().addListener((changeListener, oldVal, newVal) ->
        {
            // This call back should be running on a background thread. Perform contents on JavaFX application thread.
            Platform.runLater(() ->
            {
                userField.setText(model.getUsername());
                passwordField.setText(model.getPassword());

                // Put focus on first required field that is empty.
                if (userField.getText().isEmpty())
                    userField.requestFocus();
                else if (passwordField.getText().isEmpty())
                    passwordField.requestFocus();
                else
                    titleField.requestFocus();
            });
        });

        userField.requestFocus();
        if (LogbookUIPreferences.save_credentials) {
            model.fetchStoredUserCredentials();
        }

        textArea.textProperty().bindBidirectional(textAreaContent);
        textAreaContent.set(model.getText());
        textAreaContent.addListener((observableValue, s, t1) -> model.setText(t1));

        titleField.textProperty().setValue(model.getTitle());

        setFieldActions();
        setTextActions();
    }

    @FXML
    public void setLevel() {
        model.setLevel(levelSelector.getSelectionModel().getSelectedItem());
    }

    /**
     * Set the username and password fields to update the model's username and password fields on text entry.
     */
    private void setFieldActions() {
        userField.setOnKeyReleased(event ->
        {
            model.setUser(userField.getText());
        });

        passwordField.setOnKeyReleased(event ->
        {
            model.setPassword(passwordField.getText());
        });
    }

    /**
     * Set the title field to update the model's title and text fields on text entry
     */
    private void setTextActions() {
        titleField.setOnKeyReleased(event ->
        {
            model.setTitle(titleField.getText());
        });
    }

    @FXML
    public void embedImage() {
        EmbedImageDialog embedImageDialog = new EmbedImageDialog();
        Optional<EmbedImageDescriptor> descriptor = embedImageDialog.showAndWait();
        if (descriptor.isPresent()) {
            // Add to model
            model.addEmbeddedImage(descriptor.get());
            // Insert markup at caret position
            int caretPosition = textArea.getCaretPosition();
            String imageMarkup =
                    "![](attachment/" + descriptor.get().getId() + ")"
                            + "{width=" + descriptor.get().getWidth()
                            + " height=" + descriptor.get().getHeight() + "} ";
            textArea.insertText(caretPosition, imageMarkup);
        }
    }

    @FXML
    public void showHelp(){
        String url =
            LogService.getInstance().getLogFactories().get(LogbookUIPreferences.logbook_factory).getLogClient().getServiceUrl();
        if(url.endsWith("/")){
            url = url.substring(0, url.length() - 1);
        }
        // Need to get rid of Olog path element under which all REST endpoints are published.
        // The help file however is published directly on the context root.
        int idx = url.indexOf("/Olog");
        String helpUrl = url.substring(0, idx) + "/" + LogbookUIPreferences.markup_help;
        Platform.runLater(() -> PhoebusApplication.INSTANCE.getHostServices().showDocument(helpUrl));
    }
}

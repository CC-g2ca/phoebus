/*
 * Copyright (C) 2020 European Spallation Source ERIC.
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
 */

package org.csstudio.trends.databrowser3.ui.properties;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import org.csstudio.trends.databrowser3.Messages;
import org.csstudio.trends.databrowser3.model.Model;
import org.phoebus.logbook.ui.write.AttachmentsViewController;
import org.phoebus.logbook.ui.write.FieldsViewController;
import org.phoebus.logbook.ui.write.LogEntryCompletionHandler;
import org.phoebus.logbook.ui.write.LogEntryEditorController;
import org.phoebus.logbook.ui.write.LogEntryEditorStage;
import org.phoebus.logbook.ui.write.LogEntryModel;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tab component offering the user tools to calculate trace statistics. The calculations must
 * be triggered manually through a button, i.e. they are not performed in real time as traces are being
 * updated with new samples.
 */
public class StatisticsTab extends Tab {

    public StatisticsTab(Model model){
        super(Messages.StatisticsTab);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(this.getClass().getResource("StatisticsTab.fxml"));
        fxmlLoader.setControllerFactory(clazz -> {
            try {
                StatisticsTabController statisticsTabController =
                        (StatisticsTabController) clazz.getConstructor(Model.class)
                                .newInstance(model);
                return statisticsTabController;

            } catch (Exception e) {
                Logger.getLogger(StatisticsTabController.class.getName()).log(Level.SEVERE, "Failed to construct controller statistics tab", e);
            }
            return null;
        });

        try{
            Node node = fxmlLoader.load();
            setContent(node);
        }
        catch(IOException e){
            Logger.getLogger(StatisticsTabController.class.getName()).log(Level.SEVERE, "Failed to load FXML for statistics tab", e);
        }
    }
}
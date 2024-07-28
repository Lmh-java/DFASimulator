package io.github.lmhjava.ui.controller;

import io.github.lmhjava.ui.model.GlobalContext;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Root controller for the entire APP
 */
public class AppController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        GlobalContext.controllers.put(this.getClass().getSimpleName(), this);
    }
}

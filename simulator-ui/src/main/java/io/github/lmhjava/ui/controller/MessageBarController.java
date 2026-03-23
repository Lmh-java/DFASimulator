package io.github.lmhjava.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MessageBarController extends BaseAppController {
	private static final String INFO_STYLE = "-fx-background-color: #f3f4f6; -fx-text-fill: #111827; -fx-padding: 6 10 6 10;";
	private static final String SUCCESS_STYLE = "-fx-background-color: #dcfce7; -fx-text-fill: #14532d; -fx-padding: 6 10 6 10;";
	private static final String ERROR_STYLE = "-fx-background-color: #fee2e2; -fx-text-fill: #7f1d1d; -fx-padding: 6 10 6 10;";

	@FXML
	private Label messageLabel;

	public void showInfo(String message) {
		messageLabel.setStyle(INFO_STYLE);
		messageLabel.setText(message);
	}

	public void showSuccess(String message) {
		messageLabel.setStyle(SUCCESS_STYLE);
		messageLabel.setText(message);
	}

	public void showError(String message) {
		messageLabel.setStyle(ERROR_STYLE);
		messageLabel.setText(message);
	}
}

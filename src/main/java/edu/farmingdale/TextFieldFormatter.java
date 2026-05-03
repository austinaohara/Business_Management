package edu.farmingdale;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TextFieldFormatter {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d*");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("\\d*(\\.\\d*)?");
    private static final Pattern DIGITS_ONLY     = Pattern.compile("[^\\d]");

    // numbers only, no letters or symbols
    public static void applyIntegerFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (INTEGER_PATTERN.matcher(change.getControlNewText()).matches()) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // same as above but allows a decimal point for prices
    public static void applyDecimalFilter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            if (DECIMAL_PATTERN.matcher(change.getControlNewText()).matches()) return change;
            return null;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // just type the numbers and the slashes show up on their own, e.g. 12252025 -> 12/25/2025
    public static void applyDateFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String digits = DIGITS_ONLY.matcher(change.getControlNewText()).replaceAll("");
            if (digits.length() > 8) return null;
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 2 || i == 4) formatted.append('/');
                formatted.append(digits.charAt(i));
            }
            change.setRange(0, change.getControlText().length());
            change.setText(formatted.toString());
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }

    // same idea, type digits and the phone format fills itself in, e.g. 8005551234 -> (800) 555-1234
    public static void applyPhoneFormatter(TextField field) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String digits = DIGITS_ONLY.matcher(change.getControlNewText()).replaceAll("");
            if (digits.length() > 10) return null;
            StringBuilder formatted = new StringBuilder();
            for (int i = 0; i < digits.length(); i++) {
                if (i == 0) formatted.append('(');
                if (i == 3) formatted.append(") ");
                if (i == 6) formatted.append('-');
                formatted.append(digits.charAt(i));
            }
            change.setRange(0, change.getControlText().length());
            change.setText(formatted.toString());
            change.setCaretPosition(formatted.length());
            change.setAnchor(formatted.length());
            return change;
        };
        field.setTextFormatter(new TextFormatter<>(filter));
    }
}

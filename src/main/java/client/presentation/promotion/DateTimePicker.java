package client.presentation.promotion;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.Date;

public class DateTimePicker extends JPanel {

    private final JDateChooser date = new JDateChooser();
    private final JComboBox<Integer> cbHour = new JComboBox<>();
    private final JComboBox<Integer> cbMin = new JComboBox<>();

    public DateTimePicker() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));

        date.setDateFormatString("dd/MM/yyyy");
        date.setPreferredSize(new Dimension(140, 28));

        for (int h = 0; h < 24; h++) cbHour.addItem(h);
        for (int m = 0; m < 60; m += 5) cbMin.addItem(m);

        add(date);
        add(new JLabel("Giờ:"));
        add(cbHour);
        add(new JLabel("Phút:"));
        add(cbMin);
    }

    public void setDateTime(LocalDateTime ldt) {
        if (ldt == null) return;
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        date.setDate(d);
        cbHour.setSelectedItem(ldt.getHour());
        cbMin.setSelectedItem(ldt.getMinute() - (ldt.getMinute() % 5));
    }

    public LocalDateTime getDateTime() {
        Date d = date.getDate();
        if (d == null) return null;

        LocalDate day = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int h = (Integer) cbHour.getSelectedItem();
        int m = (Integer) cbMin.getSelectedItem();

        return LocalDateTime.of(day, LocalTime.of(h, m));
    }
}
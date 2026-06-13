
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Type;   // <-- keep THIS
import java.util.ArrayList;
import java.util.List;

public class JobTrackerSwing extends JFrame {
    
    private final String SAVE_FILE = "applications.json";
    private List<JobApplication> applications = new ArrayList<>();
    private JTable mainTable;
    private DefaultTableModel tableModel;

    public JobTrackerSwing() {
        loadApplications();

        setTitle("Job Application Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("All Applications", buildMainPanel());
        tabs.add("Active", buildActivePanel());
        tabs.add("Analytics", buildAnalyticsPanel());

        add(tabs);
        setLocationRelativeTo(null);
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveApplications();
            }
        });
    }

    private JPanel buildMainPanel() {
        String[] cols = {"Company", "Date", "Position", "Resume", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        mainTable = new JTable(tableModel);
        refreshTable();

        JScrollPane scrollPane = new JScrollPane(mainTable);

        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchApplications(searchField.getText()));

        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> {
            JobApplication app = showAppDialog(null);
            if (app != null) {
                applications.add(app);
                refreshTable();
            }
        });

        JButton editBtn = new JButton("Edit");
        editBtn.addActionListener(e -> {
            int row = mainTable.getSelectedRow();
            if (row >= 0) {
                JobApplication selected = applications.get(row);
                JobApplication updated = showAppDialog(selected);
                if (updated != null) {
                    applications.set(row, updated);
                    refreshTable();
                }
            }
        });

        JButton removeBtn = new JButton("Remove");
        removeBtn.addActionListener(e -> {
            int row = mainTable.getSelectedRow();
            if (row >= 0) {
                applications.remove(row);
                refreshTable();
            }
        });


        
        JPanel controls = new JPanel();
        controls.add(searchField);
        controls.add(searchBtn);
        controls.add(addBtn);
        controls.add(editBtn);
        controls.add(removeBtn);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controls, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildActivePanel() {
        String[] cols = {"Company", "Date", "Position", "Resume", "Status"};
        DefaultTableModel activeModel = new DefaultTableModel(cols, 0);
        JTable activeTable = new JTable(activeModel);

        for (JobApplication app : applications) {
            if (app.getStatus() == Status.OA || app.getStatus() == Status.INTERVIEWING) {
                activeModel.addRow(new Object[]{
                        app.getCompany(), app.getDate(), app.getPositionName(),
                        app.getResume(), app.getStatus()
                });
            }
        }

        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(activeTable), BorderLayout.CENTER);
        }};
    }

    private JPanel buildAnalyticsPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (applications.isEmpty()) {
                    g.drawString("No data available", 50, 50);
                    return;
                }

                int total = applications.size();
                int[] counts = new int[Status.values().length];
                for (JobApplication app : applications) {
                    counts[app.getStatus().ordinal()]++;
                }

                int startAngle = 0;
                Color[] colors = {Color.GRAY, Color.BLUE, Color.ORANGE, Color.RED, Color.GREEN};
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] > 0) {
                        int angle = (int) Math.round(360.0 * counts[i] / total);
                        g.setColor(colors[i % colors.length]);
                        g.fillArc(100, 100, 200, 200, startAngle, angle);
                        startAngle += angle;
                        g.setColor(Color.BLACK);
                        g.drawString(Status.values()[i] + ": " + counts[i], 320, 120 + i * 20);
                    }
                }
            }
        };
    }

    private void refreshTable() {//fills table with all apps in the list 
        tableModel.setRowCount(0);
        for (JobApplication app : applications) {
            tableModel.addRow(new Object[]{
                    app.getCompany(),
                    app.getDate(),
                    app.getPositionName(),
                    app.getResume(),
                    app.getStatus()
            });
        }
    }

    private void searchApplications(String query) {//fills table with only matching titles
        tableModel.setRowCount(0);//first empties the table
        for (JobApplication app : applications) {
            if (app.getCompany().toLowerCase().contains(query.toLowerCase())) {
                tableModel.addRow(new Object[]{
                        app.getCompany(), app.getDate(),
                        app.getPositionName(), app.getResume(),
                        app.getStatus()
                });
            }
        }
    }

    private JobApplication showAppDialog(JobApplication existing) {
        JTextField companyField = new JTextField(existing != null ? existing.getCompany() : "");
        JTextField dateField = new JTextField(existing != null ? existing.getDate() : "");
        JTextField posField = new JTextField(existing != null ? existing.getPositionName() : "");
        JTextField resumeField = new JTextField(existing != null ? existing.getResume() : "");
        JComboBox<Status> statusBox = new JComboBox<>(Status.values());
        if (existing != null) statusBox.setSelectedItem(existing.getStatus());

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Company:")); panel.add(companyField);
        panel.add(new JLabel("Date:")); panel.add(dateField);
        panel.add(new JLabel("Position:")); panel.add(posField);
        panel.add(new JLabel("Resume:")); panel.add(resumeField);
        panel.add(new JLabel("Status:")); panel.add(statusBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Application",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new JobApplication(
                    companyField.getText(),
                    dateField.getText(),
                    posField.getText(),
                    resumeField.getText(),
                    (Status) statusBox.getSelectedItem()
            );
        }
        return null;
    }

    private void loadApplications() {
        try (Reader reader = new FileReader(SAVE_FILE)) {
            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<JobApplication>>() {}.getType();
            List<JobApplication> list = new Gson().fromJson(reader, listType);
            if (list != null) applications = list;
        } catch (IOException ignored) {}
    }

    private void saveApplications() {
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            new Gson().toJson(applications, writer);
        } catch (IOException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JobTrackerSwing::new);
    }
}

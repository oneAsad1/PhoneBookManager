package oneasad;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardFrame extends JFrame {
    private String username;
    private JTable table;
    private DefaultTableModel model;

    public DashboardFrame(String username) {
        this.username = username;

        setTitle("Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        
        model = new DefaultTableModel(new String[]{"ID", "Name", "Phone", "Email"}, 0);
        table = new JTable(model);
        loadContacts();

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel formPanel = new JPanel(new GridLayout(5, 2));
        
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        JButton addButton = new JButton("Add");
        addButton.setBackground(Color.GREEN);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addContact(nameField.getText(), phoneField.getText(), emailField.getText());
                loadContacts();
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.setBackground(Color.YELLOW);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) table.getValueAt(selectedRow, 0);
                    updateContact(id, nameField.getText(), phoneField.getText(), emailField.getText());
                    loadContacts();
                }
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(Color.RED);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow >= 0) {
                    int id = (int) table.getValueAt(selectedRow, 0);
                    deleteContact(id);
                    loadContacts();
                }
            }
        });

        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(addButton);
        formPanel.add(updateButton);
        formPanel.add(deleteButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);
    }

    private void loadContacts() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM contacts WHERE user_id = (SELECT id FROM users WHERE username = ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
            model.setRowCount(0);

            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("phone"), resultSet.getString("email")});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addContact(String name, String phone, String email) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO contacts (user_id, name, phone, email) VALUES ((SELECT id FROM users WHERE username = ?), ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, name);
            statement.setString(3, phone);
            statement.setString(4, email);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateContact(int id, String name, String phone, String email) {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE contacts SET name = ?, phone = ?, email = ? WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.setInt(4, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void deleteContact(int id) {
        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM contacts WHERE id = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

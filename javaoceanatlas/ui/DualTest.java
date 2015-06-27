/**
 * 
 */
package javaoceanatlas.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
 
public class DualTest {
 
    public static void main(String args[]) {
        Runnable runner = new Runnable() {
 
            public void run() {
                JFrame frame1 = new JFrame("Left");
                JFrame frame2 = new JFrame("Right");
                frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JButton button1 = new JButton("Left");
                JButton button2 = new JButton("Right");
                frame1.add(button1, BorderLayout.SOUTH);
                frame2.add(button2, BorderLayout.SOUTH);
                JTextField field1 = new JTextField();
                JTextField field2 = new JTextField();
                frame1.add(field1, BorderLayout.CENTER);
                frame2.add(field2, BorderLayout.CENTER);
                ActionListener listener = new ActionListener() {
 
                    public void actionPerformed(ActionEvent e) {
                        JButton source = (JButton) e.getSource();
                        String text = getNewText(source);
                        if (!JOptionPane.UNINITIALIZED_VALUE.equals(text)
                                && text.trim().length() > 0) {
                            source.setText(text);
                        }
                    }
                };
                button1.addActionListener(listener);
                button2.addActionListener(listener);
                frame1.setBounds(100, 100, 200, 400);
                frame1.setVisible(true);
                frame2.setBounds(400, 100, 200, 400);
                frame2.setVisible(true);
            }
        };
        EventQueue.invokeLater(runner);
    }
 
    private static String getNewText(Component parent) {
        JOptionPane pane = new JOptionPane(
                "New label", JOptionPane.QUESTION_MESSAGE);
        pane.setWantsInput(true);
        JDialog dialog = pane.createDialog(parent, "Enter Text");
        dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.getRootPane().putClientProperty(
                "apple.awt.documentModalSheet", Boolean.TRUE);
        dialog.setVisible(true);
        return (String) pane.getInputValue();
    }
}

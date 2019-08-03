package main.java.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import javax.swing.JSplitPane;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JList;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JScrollPane;

public class DetectionWindow extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					DetectionWindow frame = new DetectionWindow();
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}

	/**
	 * Create the frame.
	 * @throws IOException 
	 */
	public DetectionWindow() throws IOException {
		setTitle("GraphicDetector-v1.0");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1302, 742);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		contentPane.add(splitPane);
		
		JList list = new JList();
		splitPane.setLeftComponent(list);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setRightComponent(scrollPane);
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("Consolas", Font.PLAIN, 20));
		scrollPane.setViewportView(textArea);
		scrollPane.setRowHeaderView(new LineNumberHeaderView());
		
		File f = new File("D:/MyClass.java");
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String content = br.readLine();
		while(content != null) {
			textArea.setText(textArea.getText()+content+"\r\n");
			content = br.readLine();
		}
		br.close();
	}

}
